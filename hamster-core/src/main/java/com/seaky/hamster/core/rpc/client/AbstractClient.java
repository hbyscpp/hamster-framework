package com.seaky.hamster.core.rpc.client;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.VersionConstans;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;
import com.seaky.hamster.core.rpc.utils.NetUtils;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaService;

import net.jodah.failsafe.CircuitBreaker;
import rx.Observable;

public abstract class AbstractClient<Req, Rsp> implements Client<Req, Rsp> {

  private static Logger logger = LoggerFactory.getLogger(AbstractClient.class);

  private ServiceReferenceDescriptorSearcher searcher = null;
  private RegisterationService registerationService;

  private ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private ClientConfig config;

  private ClientInterceptorService<Req, Rsp> interceptorSupportService;

  private ConcurrentHashMap<String, ReferenceService<Req, Rsp>> serviceCache =
      new ConcurrentHashMap<String, ReferenceService<Req, Rsp>>();

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

  private static AtomicLong seqNum = new AtomicLong(1);


  private AsynServiceExecutor<Req, Rsp> asynServiceExecutor;

  // TODO 预防lock变的越来越大，soft or weak引用??,定时清理,使用最后更新时间的时间
  private ConcurrentHashMap<String, Lock> lockMaps = new ConcurrentHashMap<String, Lock>();

  // TODO 定时清理,某个接口的断路器
  private ConcurrentHashMap<String, CircuitBreaker> circuitBreakerMaps = new ConcurrentHashMap<>();


  public ClientTransport<Req, Rsp> getTransport(final ServiceProviderDescriptor sd) {
    String key = Utils.generateKey(sd.getHost(), String.valueOf(sd.getPort()));
    ClientTransport<Req, Rsp> transport = clientCache.get(key);
    if (transport != null && !transport.isClosed()) {
      return transport;
    }
    Lock lock = findLock(key);
    try {
      lock.lock();
      transport = clientCache.get(key);
      if (transport != null) {
        if (!transport.isClosed()) {
          // 未关闭的返回，此时有可能在重连之中
          return transport;
        }
      }
      // 已经关闭了
      transport = createTransport(sd, config, getProtocolExtensionFactory(), this);
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
    // 清理期间不能连接远程服务
    Lock lock = findLock(serverAddr);
    try {
      lock.lock();
      ClientTransport<Req, Rsp> transport = clientCache.get(serverAddr);
      if (transport != null && transport.isClosed()) {
        clientCache.remove(serverAddr);
      }
    } finally {
      lock.unlock();
    }

  }


  public long getAndIncrementSeqNum() {
    return seqNum.getAndIncrement();
  }


  protected abstract ClientTransport<Req, Rsp> createTransport(ServiceProviderDescriptor sd,
      ClientConfig config, ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory,
      AbstractClient<Req, Rsp> client);

  protected AbstractClient(ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    this.protocolExtensionFactory = protocolExtensionFactory;
    this.interceptorSupportService =
        new ClientInterceptorService<Req, Rsp>(protocolExtensionFactory);
    this.searcher = new DefaultServiceReferenceDescriptorSearcher(this);
  }

  private ReferenceService<Req, Rsp> getReferenceService(String serviceName, String referenceApp,
      String referenceVersion, String referenceGroup) {
    return serviceCache
        .get(Utils.generateKey(serviceName, referenceApp, referenceVersion, referenceGroup));
  }

  private ReferenceService<Req, Rsp> addReferService(String serviceName, String referenceApp,
      String referenceVersion, String referenceGroup, ReferenceService<Req, Rsp> service) {
    ReferenceService<Req, Rsp> old = serviceCache.putIfAbsent(
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
        sd.setProtocol(getProtocolExtensionFactory().protocolName());
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
    ReferenceService<Req, Rsp> service =
        getReferenceService(serviceName, referApp, referVersion, referGroup);
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
    rd.setProtocol(getProtocolExtensionFactory().protocolName());
    rd.setMaxProtocolVersion(getProtocolExtensionFactory().protocolMaxVersion());
    long time = System.currentTimeMillis();
    rd.setRegistTime(time);
    rd.setHost(this.config.getHost());
    rd.setFrameworkVersion(VersionConstans.VERSION);
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
    service.processAsyn(null, true);
    return service;
  }

  Collection<ServiceProviderDescriptor> getAllServices(String serviceName) {

    return registerationService.findServiceProviders(serviceName);

  }

  private static class ReferenceService<Req, Rsp> implements JavaService {

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

    public SettableFuture<Object> processAsyn(Object[] params, boolean isPreConnect) {
      // 检查参数

      ReferenceServiceRequest request = new ReferenceServiceRequest();
      request.setServiceName(serviceName);
      request.setReferenceApp(referenceApp);
      request.setReferenceVersion(referenceVersion);
      request.setReferenceGroup(referenceGroup);
      request.setParams(params);
      final SettableFuture<Object> result = SettableFuture.create();
      EndpointConfig config =
          client.getEndpointConfig(serviceName, referenceApp, referenceVersion, referenceGroup);
      client.asynServiceExecutor.callService(request, result, config, isPreConnect);

      return result;
    }

    @Override
    public Observable<Object> process(Object[] request) {
      final SettableFuture<Object> f = processAsyn(request, false);
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

  public ServiceReferenceDescriptor getServiceReferenceDescriptor(String serviceName,
      String referenceApp, String referenceVersion, String referenceGroup) {
    Long time = referenceTimeCache
        .get(Utils.generateKey(serviceName, referenceApp, referenceVersion, referenceGroup));
    return registerationService.findServiceReference(referenceApp, serviceName, referenceVersion,
        referenceGroup, getProtocolExtensionFactory().protocolName(), config.getHost(),
        Utils.getCurrentVmPid(), time);
  }

  public ServiceReferenceDescriptorSearcher getSearcher() {
    return searcher;
  }

  public CircuitBreaker getCircuitBreaker(String serviceName, String app, String group,
      String version, String host, String port) {
    String key = Utils.generateKey(serviceName, app, group, version, host, port);
    CircuitBreaker cb = circuitBreakerMaps.get(key);
    return cb;
  }

  public CircuitBreaker putCircuitBreaker(String serviceName, String app, String group,
      String version, String host, String port, CircuitBreaker cb) {
    String key = Utils.generateKey(serviceName, app, group, version, host, port);
    CircuitBreaker oldcb = circuitBreakerMaps.putIfAbsent(key, cb);
    if (oldcb == null)
      return cb;
    return oldcb;
  }

  public ProtocolExtensionFactory<Req, Rsp> getProtocolExtensionFactory() {
    return protocolExtensionFactory;
  }


}
