package com.seaky.hamster.core.rpc.client;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.client.cluster.ClusterServiceFactory;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.client.router.ServiceRouter;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaReferenceService;

import io.netty.util.concurrent.ImmediateEventExecutor;
import rx.Observable;
import rx.subjects.ReplaySubject;

public abstract class AbstractClient<Req, Rsp> implements Client<Req, Rsp> {

	private static Logger logger = LoggerFactory
			.getLogger(AbstractClient.class);

	private RegisterationService registerationService;

	static ExtensionLoader<ServiceRouter> routerExtension = ExtensionLoader
			.getExtensionLoaders(ServiceRouter.class);

	static ExtensionLoader<ClusterServiceFactory> clusterExtension = ExtensionLoader
			.getExtensionLoaders(ClusterServiceFactory.class);

	static ExtensionLoader<ServiceLoadBalancer> loadBalanceExtension = ExtensionLoader
			.getExtensionLoaders(ServiceLoadBalancer.class);
	protected ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

	private ClientConfig config;

	private ClientInterceptorService<Req, Rsp> interceptorSupportService;

	private ConcurrentHashMap<String, JavaReferenceService> serviceCache = new ConcurrentHashMap<String, JavaReferenceService>();

	// key 服务端
	private ConcurrentHashMap<String, ClientTransport<Req, Rsp>> clientCache = new ConcurrentHashMap<String, ClientTransport<Req, Rsp>>();

	// key 服务端－客户端
	private ConcurrentHashMap<String, ServiceReferenceDescriptor> referenceCache = new ConcurrentHashMap<String, ServiceReferenceDescriptor>();

	private ConcurrentHashMap<ProcessPhase, ConcurrentHashMap<String, List<ServiceInterceptor>>> allservicesInterceptorsBeforeCluster = new ConcurrentHashMap<ProcessPhase, ConcurrentHashMap<String, List<ServiceInterceptor>>>();

	// 客户端是否启动
	private boolean isRunning;

	private AtomicLong seqNum = new AtomicLong(1);

	// TODO 预防lock变的越来越大，soft or weak引用??,定时清理
	private ConcurrentHashMap<String, Lock> lockMaps = new ConcurrentHashMap<String, Lock>();

	private AsynServiceExecutor<Req, Rsp> asynServiceExecutor;

	// 获取连接,可能需调用connect方法 TODO 动态反馈远程服务的状态,比如调用多少次 是由于网络失败，一段时间内变为
	//获取transport，可能处于未连接状态
	public ClientTransport<Req, Rsp> getTransport(
			final ServiceProviderDescriptor sd) {
		String key = Utils.generateKey(sd.getHost(),
				String.valueOf(sd.getPort()));
		ClientTransport<Req, Rsp> transport = clientCache.get(key);
		if (transport != null) {
			if (!transport.isClosed()) {
				return transport;
			}
		}
		Lock lock = findLock(key);
		try {
			lock.lock();
			transport = clientCache.get(key);
			if (transport != null) {
				if (!transport.isClosed()) {
					return transport;
				}
			}
			// 已经关闭了
			clientCache.remove(key);
			transport = createTransport(sd, config, protocolExtensionFactory,
					this);
			clientCache.put(key, transport);
			return transport;
		} finally {
			lock.unlock();
		}

	}

	private Lock findLock(String key) {
		Lock lock = lockMaps.get(key);
		if (lock == null) {
			lock = new ReentrantLock();
			Lock oldlock = lockMaps.putIfAbsent(key, lock);
			if (oldlock != null) {
				lock = oldlock;
			}
		}
		return lock;
	}

	// 更新
	public void updateReferenceDescriptor(ServiceContext sc) {
		String referKey = Utils.generateKey(
				ServiceContextUtils.getServiceName(sc),
				ServiceContextUtils.getReferenceApp(sc),
				ServiceContextUtils.getReferenceVersion(sc),
				ServiceContextUtils.getReferenceGroup(sc));
		//TODO 从注册中心获取
		ServiceReferenceDescriptor rd = referenceCache.get(referKey);
		if (rd == null)
			throw new RuntimeException("can not found reference descriptor "
					+ referKey);
		String pair = Utils.createServerAndClientAddress(ServiceContextUtils.getClientHost(sc), ServiceContextUtils.getClientPort(sc), ServiceContextUtils.getServerHost(sc), ServiceContextUtils.getServerPort(sc));
		
		if (!rd.containPair(pair)) {
			//TODO 
			rd.addAddressPair(pair);
			registerationService.registReference(rd);
		}
	}

	public void removeReference(InetSocketAddress serverAddress,
			InetSocketAddress clientAddress) {
		String serverAddr = Utils.generateKey(serverAddress.getAddress()
				.getHostAddress(), String.valueOf(serverAddress.getPort()));
		String clientAddr = Utils.generateKey(clientAddress.getAddress()
				.getHostAddress(), String.valueOf(clientAddress.getPort()));
		String sb = Utils.createServerAndClientAddress(clientAddress.getAddress()
				.getHostAddress(), clientAddress.getPort(), serverAddress.getAddress()
				.getHostAddress(), serverAddress.getPort());
		// 清理期间不能连接远程服务
		Lock lock = findLock(serverAddr);
		try {
			
			lock.lock();
			ClientTransport<Req, Rsp> transport = clientCache.get(serverAddr);
			if (transport != null && transport.isConnected()) {
				String clientkey = Utils.generateKey(transport
						.getLocalAddress().getAddress().getHostAddress(),
						String.valueOf(transport.getLocalAddress().getPort()));
				// 客户端和服务端相同，并且保持连接,transport关闭后，恰好使用同一个端口和同一个服务端去连接,不做处理
				if (clientAddr.equals(clientkey))
					return;

				removeReferByAddr(sb.toString());

				return;
			} else {
				// 删除
				removeReferByAddr(sb.toString());
				clientCache.remove(serverAddr);
			}
		} finally {
			lock.unlock();
		}

	}

	private void removeReferByAddr(String addrpairs) {
		for (ServiceReferenceDescriptor rd : referenceCache.values()) {
			if (rd.getAddressPairs().contains(rd)) {
				rd.getAddressPairs().remove(rd);
				// 主动更新
				registerationService.registReference(rd);
			}
		}
	}

	public long getAndIncrementSeqNum() {
		return seqNum.getAndIncrement();
	}

	protected abstract ClientTransport<Req, Rsp> createTransport(
			ServiceProviderDescriptor sd, ClientConfig config,
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory,
			AbstractClient<Req, Rsp> client);

	protected AbstractClient(
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
		this.protocolExtensionFactory = protocolExtensionFactory;
		this.interceptorSupportService = new ClientInterceptorService<Req, Rsp>(
				protocolExtensionFactory);
	}

	private JavaReferenceService getReferenceService(String serviceName,
			String referenceApp, String referenceVersion, String referenceGroup) {
		return serviceCache.get(Utils.generateKey(serviceName, referenceApp,
				referenceVersion, referenceGroup));
	}

	private JavaReferenceService addReferService(String serviceName,
			String referenceApp, String referenceVersion,
			String referenceGroup, JavaReferenceService service) {
		JavaReferenceService old = serviceCache.putIfAbsent(Utils.generateKey(
				serviceName, referenceApp, referenceVersion, referenceGroup),
				service);
		if (old != null)
			return old;
		return service;
	}

	private void removeReferService(String serviceName, String referenceApp,
			String referenceVersion, String referenceGroup) {
		serviceCache.remove(Utils.generateKey(serviceName, referenceApp,
				referenceVersion, referenceGroup));
	}

	@Override
	public synchronized void connect(RegisterationService registerationService,
			ClientConfig config) {
		if (registerationService == null || config == null)
			throw new IllegalArgumentException("argument can not be null");
		if (isRunning)
			throw new RuntimeException("client is starting");
		this.registerationService = registerationService;
		this.config = config;
		this.asynServiceExecutor = new AsynServiceExecutor<Req, Rsp>(this);
		ClientResourceManager.start();
		this.isRunning = true;

	}

	@Override
	public synchronized void close() {
		if (isRunning) {

			for (ServiceReferenceDescriptor rd : referenceCache.values()) {
				registerationService.unregistReference(rd);
			}

			for (ClientTransport<Req, Rsp> transport : clientCache.values()) {

				try {
					transport.close();
				} catch (Exception e) {
					logger.error("close transport error ", e);
				}
			}
			serviceCache.clear();
			clientCache.clear();
			referenceCache.clear();
			isRunning = false;

		}
	}

	@Override
	public JavaReferenceService findReferenceService(String serviceName,
			String referApp, String version, String group) {
		if (referApp == null || serviceName == null || version == null)
			return null;
		return getReferenceService(serviceName, referApp, version, group);
	}

	@Override
	public synchronized JavaReferenceService reference(EndpointConfig config) {
		if (config == null)
			throw new RuntimeException("refer config can not be null");
		String referApp = config.get(ConfigConstans.REFERENCE_APP);
		if (referApp == null || !referApp.matches(Constants.APP_NAME_ALLOW_REG)) {
			throw new IllegalArgumentException(
					"Reference app name contains special char,app name must compose of [0-9 _ -. $ a-z A-Z],but app name is "
							+ referApp);
		}

		String serviceName = config.get(ConfigConstans.REFERENCE_NAME);
		if (serviceName == null
				|| !serviceName.matches(Constants.SERVICE_NAME_ALLOW_REG)) {
			throw new IllegalArgumentException(
					"service name contains special char,service name must compose of [0-9 _ . $ a-z A-Z],but service name is "
							+ serviceName);
		}
		String version = config.get(ConfigConstans.REFERENCE_VERSION);

		if (version == null
				|| !version.matches(Constants.VERSION_NAME_ALLOW_REG)) {
			throw new IllegalArgumentException(
					"service version contains special char,service version must compose of [0-9.],but version is "
							+ version);
		}
		Utils.checkVersionFormat(version);
		String group = config.get(ConfigConstans.REFERENCE_GROUP);
		if (group == null || !group.matches(Constants.GROUP_NAME_ALLOW_REG)) {
			throw new IllegalArgumentException(
					"service version contains special char,service version must compose of [a-zA-Z0-9_,],but group is "
							+ group);
		}
		JavaReferenceService service = getReferenceService(serviceName,
				referApp, version, group);
		if (service != null) {
			return service;
		}

		if (config.getValueAsBoolean(ConfigConstans.REFERENCE_CHECK_SERVICE,
				ConfigConstans.REFERENCE_CHECK_SERVICE_DEFAULT)) {

			Collection<ServiceProviderDescriptor> services = getAllServices(serviceName);
			if (services == null || services.size() == 0)
				throw new RuntimeException("no " + serviceName
						+ " provider found");
		}
		EndpointConfig copyOfConfig = config.deepCopy();

		service = new ReferenceService<Req, Rsp>(this, referApp, serviceName,
				version, group);
		service = addReferService(serviceName, referApp, version, group,
				service);
		ServiceReferenceDescriptor rd = new ServiceReferenceDescriptor();
		rd.setConfig(copyOfConfig);
		rd.setPid(Utils.getCurrentVmPid());
		rd.setProtocol(protocolExtensionFactory.protocolName());
		rd.setRegistTime(System.currentTimeMillis());
		referenceCache.put(
				Utils.generateKey(serviceName, referApp, version, group), rd);
		try {
			String interceptors = config
					.get(ConfigConstans.REFERENCE_INTERCEPTORS);
			registerationService.registReference(rd);
			addServiceInterceptors(serviceName, referApp, version, group,
					interceptors, ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
			addServiceInterceptors(serviceName, referApp, version, group,
					interceptors, ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
		} catch (Exception e) {
			removeReferService(serviceName, referApp, version, group);
			referenceCache.remove(Utils.generateKey(serviceName, referApp,
					version, group));
			Utils.throwException(e);
		}
		return service;
	}

	Collection<ServiceProviderDescriptor> getAllServices(String serviceName) {

		return registerationService.findServices(serviceName);

	}

	public static class ReferenceService<Req, Rsp> implements
			JavaReferenceService {

		private AbstractClient<Req, Rsp> client;

		private String serviceName;

		private String referenceApp;

		private String referenceVersion;

		private String referenceGroup;

		public ReferenceService(AbstractClient<Req, Rsp> client,
				String referenceApp, String serviceName,
				String referenceVersion, String referenceGroup) {
			this.client = client;
			this.serviceName = serviceName;
			this.referenceApp = referenceApp;
			this.referenceGroup = referenceGroup;
			this.referenceVersion = referenceVersion;
		}

		@Override
		public SettableFuture<Object> processAsyn(Object[] params) {
			// 检查参数

			ReferenceServiceRequest request = new ReferenceServiceRequest();
			request.setServiceName(serviceName);
			request.setReferenceApp(referenceApp);
			request.setReferenceVersion(referenceVersion);
			request.setReferenceGroup(referenceGroup);
			request.setParams(params);
			SettableFuture<Object> result = SettableFuture.create();
			client.asynServiceExecutor.callService(request, result, client
					.getEndpointConfig(serviceName, referenceApp,
							referenceVersion, referenceGroup));
			return result;
		}

		@Override
		public Object process(Object[] request) throws Exception {
			try {
				return processAsyn(request).get();
			} catch (InterruptedException e) {
				throw e;
			} catch (ExecutionException e) {
				Utils.throwException(e.getCause());
			} catch (Exception e) {
				throw e;
			}
			throw new NotSetResultException();
		}

		@Override
		public Observable<Object> processReactive(Object[] request) {
			final SettableFuture<Object> f = processAsyn(request);
			final ReplaySubject<Object> subject = ReplaySubject.create(1);
			f.addListener(new Runnable() {
				public void run() {
					try {
						Object o = f.get();
						subject.onNext(o);
						subject.onCompleted();
					} catch (InterruptedException e) {
						subject.onError(e);
					} catch (ExecutionException e) {
						subject.onError(e.getCause());
					}
				}
			}, ImmediateEventExecutor.INSTANCE);
			return subject;
		}
	}

	public ClientInterceptorService<Req, Rsp> getClientInterceptorService() {
		return interceptorSupportService;
	}

	public RegisterationService getRegisterationService() {
		return registerationService;
	}

	private void addServiceInterceptors(String serviceName, String referApp,
			String version, String group, String interceptors,
			ProcessPhase phase) {
		List<ServiceInterceptor> cinterceptors = Utils.extractByProcessPhase(
				interceptors, phase);

		ConcurrentHashMap<String, List<ServiceInterceptor>> serviceInterceptorsMap = allservicesInterceptorsBeforeCluster
				.get(phase);

		if (serviceInterceptorsMap == null) {
			serviceInterceptorsMap = new ConcurrentHashMap<String, List<ServiceInterceptor>>();
			ConcurrentHashMap<String, List<ServiceInterceptor>> oldserviceInterceptorsMap = allservicesInterceptorsBeforeCluster
					.putIfAbsent(phase, serviceInterceptorsMap);

			if (oldserviceInterceptorsMap != null)
				serviceInterceptorsMap = oldserviceInterceptorsMap;
		}
		serviceInterceptorsMap.put(
				Utils.generateKey(serviceName, referApp, version, group),
				cinterceptors);

	}

	public List<ServiceInterceptor> getServiceInterceptors(String serviceName,
			String referenceApp, String referenceVersion,
			String referenceGroup, ProcessPhase phase) {
		ConcurrentHashMap<String, List<ServiceInterceptor>> serviceInterceptorsMap = allservicesInterceptorsBeforeCluster
				.get(phase);
		if (serviceInterceptorsMap == null)
			return null;

		return serviceInterceptorsMap.get(Utils.generateKey(serviceName,
				referenceApp, referenceVersion, referenceGroup));
	}

	private EndpointConfig getEndpointConfig(String serviceName,
			String referenceApp, String referenceVersion, String referenceGroup) {
		return referenceCache.get(
				Utils.generateKey(serviceName, referenceApp, referenceVersion,
						referenceGroup)).getConfig();
	}

}
