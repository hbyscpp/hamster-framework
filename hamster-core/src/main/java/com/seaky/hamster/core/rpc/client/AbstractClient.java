package com.seaky.hamster.core.rpc.client;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;
import com.seaky.hamster.core.rpc.utils.NetUtils;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaService;

import rx.Observable;

public abstract class AbstractClient<Req, Rsp> implements Client<Req, Rsp> {

  private static Logger logger = LoggerFactory.getLogger(AbstractClient.class);

  private RegisterationService registerationService;

  protected ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private ClientConfig config;

  private ClientInterceptorService<Req, Rsp> interceptorSupportService;

  private ConcurrentHashMap<String, JavaService> serviceCache =
      new ConcurrentHashMap<String, JavaService>();

  // key 服务端
  private ConcurrentHashMap<String, ClientTransport<Req, Rsp>> clientCache =
      new ConcurrentHashMap<String, ClientTransport<Req, Rsp>>();

  // 引用时间的缓存
  private ConcurrentHashMap<String, Long> referenceTimeCache =
      new ConcurrentHashMap<String, Long>();

  // 服务拦截器的配置
  private ConcurrentHashMap<ProcessPhase, ConcurrentHashMap<String, List<ServiceInterceptor>>> allservicesInterceptorsBeforeCluster =
      new ConcurrentHashMap<ProcessPhase, ConcurrentHashMap<String, List<ServiceInterceptor>>>();

  // 客户端是否启动
  private boolean isRunning;

  private AtomicLong seqNum = new AtomicLong(1);

  private AtomicLong traceId = new AtomicLong(1);


  // TODO 预防lock变的越来越大，soft or weak引用??,定时清理
  private ConcurrentHashMap<String, Lock> lockMaps = new ConcurrentHashMap<String, Lock>();

  private AsynServiceExecutor<Req, Rsp> asynServiceExecutor;

  // 获取连接,可能需调用connect方法 TODO 动态反馈远程服务的状态,比如调用多少次 是由于网络失败，一段时间内变为
  // 获取transport，可能处于未连接状态
  public ClientTransport<Req, Rsp> getTransport(final ServiceProviderDescriptor sd) {
    String key = Utils.generateKey(sd.getHost(), String.valueOf(sd.getPort()));
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
      transport = createTransport(sd, config, protocolExtensionFactory, this);
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


  public void removeReference(InetSocketAddress serverAddress, InetSocketAddress clientAddress) {
    String serverAddr = Utils.generateKey(serverAddress.getAddress().getHostAddress(),
        String.valueOf(serverAddress.getPort()));
    String clientAddr = Utils.generateKey(clientAddress.getAddress().getHostAddress(),
        String.valueOf(clientAddress.getPort()));
    // 清理期间不能连接远程服务
    Lock lock = findLock(serverAddr);
    try {
      lock.lock();
      ClientTransport<Req, Rsp> transport = clientCache.get(serverAddr);
      if (transport != null && transport.isConnected()) {
        String clientkey =
            Utils.generateKey(transport.getLocalAddress().getAddress().getHostAddress(),
                String.valueOf(transport.getLocalAddress().getPort()));
        // 客户端和服务端相同，并且保持连接,transport关闭后，恰好使用同一个端口和同一个服务端去连接,不做处理
        if (clientAddr.equals(clientkey))
          return;
      } else {
        // 删除
        clientCache.remove(serverAddr);
      }
    } finally {
      lock.unlock();
    }

  }


  public long getAndIncrementSeqNum() {
    return seqNum.getAndIncrement();
  }

  public long getAndIncrementTraceId() {
    return traceId.getAndIncrement();
  }

  protected abstract ClientTransport<Req, Rsp> createTransport(ServiceProviderDescriptor sd,
      ClientConfig config, ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory,
      AbstractClient<Req, Rsp> client);

  protected AbstractClient(ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    this.protocolExtensionFactory = protocolExtensionFactory;
    this.interceptorSupportService =
        new ClientInterceptorService<Req, Rsp>(protocolExtensionFactory);
  }

  private JavaService getReferenceService(String serviceName, String referenceApp,
      String referenceVersion, String referenceGroup) {
    return serviceCache
        .get(Utils.generateKey(serviceName, referenceApp, referenceVersion, referenceGroup));
  }

  private JavaService addReferService(String serviceName, String referenceApp,
      String referenceVersion, String referenceGroup, JavaService service) {
    JavaService old = serviceCache.putIfAbsent(
        Utils.generateKey(serviceName, referenceApp, referenceVersion, referenceGroup), service);
    if (old != null)
      return old;
    return service;
  }

  private void removeReferService(String serviceName, String referenceApp, String referenceVersion,
      String referenceGroup) {
    serviceCache
        .remove(Utils.generateKey(serviceName, referenceApp, referenceVersion, referenceGroup));
  }

  @Override
  public synchronized void connect(RegisterationService registerationService, ClientConfig config) {
    if (registerationService == null || config == null)
      throw new IllegalArgumentException("argument can not be null");
    if (isRunning)
      throw new RuntimeException("client is starting");
    this.registerationService = registerationService;
    this.config = config;

    if (StringUtils.isBlank(config.getHost())) {
      this.config.setHost(NetUtils.getLocalHost());
    }
    this.asynServiceExecutor = new AsynServiceExecutor<Req, Rsp>(this);
    ClientResourceManager.start();
    this.isRunning = true;

  }

  @Override
  public synchronized void close() {
    if (isRunning) {

      for (String name : serviceCache.keySet()) {
        String[] attrs = name.split(Constants.TILDE_LINE);
        ServiceReferenceDescriptor sd = new ServiceReferenceDescriptor();
        sd.setHost(config.getHost());
        sd.setServiceName(attrs[0]);
        sd.setReferApp(attrs[1]);
        sd.setReferVersion(attrs[2]);
        sd.setReferGroup(attrs[3]);
        sd.setProtocol(protocolExtensionFactory.protocolName());
        sd.setPid(Utils.getCurrentVmPid());
        try {
          sd = getServiceReferenceDescriptor(sd.getServiceName(), sd.getReferApp(),
              sd.getReferVersion(), sd.getReferGroup());
          registerationService.unregistServiceReference(sd);
          logger.info("unbound service reference {}:{}:{}:{},on {}:{} ", attrs[0], attrs[1],
              attrs[2], attrs[3], config.getHost(), sd.getPid());
        } catch (Exception e) {
          logger.error("unregist service {}  error ", sd.toString(), e);
        }
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
      referenceTimeCache.clear();
      allservicesInterceptorsBeforeCluster.clear();
      isRunning = false;

    }
  }

  @Override
  public JavaService findReferenceService(String serviceName, String referApp, String version,
      String group) {
    if (referApp == null || serviceName == null || version == null)
      return null;
    return getReferenceService(serviceName, referApp, version, group);
  }

  @Override
  public synchronized JavaService reference(EndpointConfig config) {
    if (config == null)
      throw new RuntimeException("refer config can not be null");
    String referApp = config.get(ConfigConstans.REFERENCE_APP);
    if (referApp == null || !referApp.matches(Constants.APP_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "Reference app name contains special char,app name must compose of [0-9 _ -. $ a-z A-Z],but app name is "
              + referApp);
    }

    String serviceName = config.get(ConfigConstans.REFERENCE_NAME);
    if (serviceName == null || !serviceName.matches(Constants.SERVICE_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service name contains special char,service name must compose of [0-9 _ . $ a-z A-Z],but service name is "
              + serviceName);
    }
    String referVersion = config.get(ConfigConstans.REFERENCE_VERSION);

    if (referVersion == null || !referVersion.matches(Constants.VERSION_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service version contains special char,service version must compose of [0-9.],but version is "
              + referVersion);
    }
    Utils.checkVersionFormat(referVersion);
    String referGroup = config.get(ConfigConstans.REFERENCE_GROUP);
    if (referGroup == null || !referGroup.matches(Constants.GROUP_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service version contains special char,service version must compose of [a-zA-Z0-9_,],but group is "
              + referGroup);
    }
    JavaService service = getReferenceService(serviceName, referApp, referVersion, referGroup);
    if (service != null) {
      return service;
    }

    if (config.getValueAsBoolean(ConfigConstans.REFERENCE_CHECK_SERVICE,
        ConfigConstans.REFERENCE_CHECK_SERVICE_DEFAULT)) {

      Collection<ServiceProviderDescriptor> services = getAllServices(serviceName);
      if (services == null || services.size() == 0)
        throw new RuntimeException("no " + serviceName + " provider found");
    }
    EndpointConfig copyOfConfig = config.deepCopy();

    service = new ReferenceService<Req, Rsp>(this, referApp, serviceName, referVersion, referGroup);
    service = addReferService(serviceName, referApp, referVersion, referGroup, service);
    ServiceReferenceDescriptor rd = new ServiceReferenceDescriptor();
    rd.setConfig(copyOfConfig);
    rd.setPid(Utils.getCurrentVmPid());
    rd.setProtocol(protocolExtensionFactory.protocolName());
    long time = System.currentTimeMillis();
    rd.setRegistTime(time);
    rd.setHost(this.config.getHost());
    referenceTimeCache.put(Utils.generateKey(serviceName, referApp, referVersion, referGroup),
        time);
    try {
      String interceptors = config.get(ConfigConstans.REFERENCE_INTERCEPTORS);
      registerationService.registServiceReference(rd);
      addServiceInterceptors(serviceName, referApp, referVersion, referGroup, interceptors,
          ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
      addServiceInterceptors(serviceName, referApp, referVersion, referGroup, interceptors,
          ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
    } catch (Exception e) {
      removeReferService(serviceName, referApp, referVersion, referGroup);
      removeServiceInterceptors(serviceName, referApp, referVersion, referGroup,
          ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
      removeServiceInterceptors(serviceName, referApp, referVersion, referGroup,
          ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
      referenceTimeCache.remove(Utils.generateKey(serviceName, referApp, referVersion, referGroup));
      Utils.throwException(e);
    }
    return service;
  }

  Collection<ServiceProviderDescriptor> getAllServices(String serviceName) {

    return registerationService.findServiceProviders(serviceName);

  }

  public static class ReferenceService<Req, Rsp> implements JavaService {

    private AbstractClient<Req, Rsp> client;

    private String serviceName;

    private String referenceApp;

    private String referenceVersion;

    private String referenceGroup;

    public ReferenceService(AbstractClient<Req, Rsp> client, String referenceApp,
        String serviceName, String referenceVersion, String referenceGroup) {
      this.client = client;
      this.serviceName = serviceName;
      this.referenceApp = referenceApp;
      this.referenceGroup = referenceGroup;
      this.referenceVersion = referenceVersion;
    }

    public SettableFuture<Object> processAsyn(Object[] params) {
      // 检查参数

      ReferenceServiceRequest request = new ReferenceServiceRequest();
      request.setServiceName(serviceName);
      request.setReferenceApp(referenceApp);
      request.setReferenceVersion(referenceVersion);
      request.setReferenceGroup(referenceGroup);
      request.setParams(params);
      SettableFuture<Object> result = SettableFuture.create();
      client.asynServiceExecutor.callService(request, result,
          client.getEndpointConfig(serviceName, referenceApp, referenceVersion, referenceGroup));
      return result;
    }

    public Object processNormal(Object[] request) throws Exception {
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
    public Observable<Object> process(Object[] request) {
      final SettableFuture<Object> f = processAsyn(request);
      return Observable.from(f);
    }
  }

  public ClientInterceptorService<Req, Rsp> getClientInterceptorService() {
    return interceptorSupportService;
  }

  public RegisterationService getRegisterationService() {
    return registerationService;
  }

  private void addServiceInterceptors(String serviceName, String referApp, String version,
      String group, String interceptors, ProcessPhase phase) {
    List<ServiceInterceptor> cinterceptors = Utils.extractByProcessPhase(interceptors, phase);

    ConcurrentHashMap<String, List<ServiceInterceptor>> serviceInterceptorsMap =
        allservicesInterceptorsBeforeCluster.get(phase);

    if (serviceInterceptorsMap == null) {
      serviceInterceptorsMap = new ConcurrentHashMap<String, List<ServiceInterceptor>>();
      ConcurrentHashMap<String, List<ServiceInterceptor>> oldserviceInterceptorsMap =
          allservicesInterceptorsBeforeCluster.putIfAbsent(phase, serviceInterceptorsMap);

      if (oldserviceInterceptorsMap != null)
        serviceInterceptorsMap = oldserviceInterceptorsMap;
    }
    serviceInterceptorsMap.put(Utils.generateKey(serviceName, referApp, version, group),
        cinterceptors);

  }

  private void removeServiceInterceptors(String serviceName, String referApp, String version,
      String group, ProcessPhase phase) {

    ConcurrentHashMap<String, List<ServiceInterceptor>> serviceInterceptorsMap =
        allservicesInterceptorsBeforeCluster.get(phase);

    if (serviceInterceptorsMap == null)
      return;
    serviceInterceptorsMap.remove(Utils.generateKey(serviceName, referApp, version, group));

  }

  public List<ServiceInterceptor> getServiceInterceptors(String serviceName, String referenceApp,
      String referenceVersion, String referenceGroup, ProcessPhase phase) {
    ConcurrentHashMap<String, List<ServiceInterceptor>> serviceInterceptorsMap =
        allservicesInterceptorsBeforeCluster.get(phase);
    if (serviceInterceptorsMap == null)
      return null;

    return serviceInterceptorsMap
        .get(Utils.generateKey(serviceName, referenceApp, referenceVersion, referenceGroup));
  }

  private EndpointConfig getEndpointConfig(String serviceName, String referenceApp,
      String referenceVersion, String referenceGroup) {
    return getServiceReferenceDescriptor(serviceName, referenceApp, referenceVersion,
        referenceGroup).getConfig();
  }

  private ServiceReferenceDescriptor getServiceReferenceDescriptor(String serviceName,
      String referenceApp, String referenceVersion, String referenceGroup) {
    Long time = referenceTimeCache
        .get(Utils.generateKey(serviceName, referenceApp, referenceVersion, referenceGroup));
    return registerationService.findServiceReference(referenceApp, serviceName, referenceVersion,
        referenceGroup, protocolExtensionFactory.protocolName(), config.getHost(),
        Utils.getCurrentVmPid(), time);
  }

}
