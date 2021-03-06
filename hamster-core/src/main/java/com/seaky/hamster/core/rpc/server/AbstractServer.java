package com.seaky.hamster.core.rpc.server;

import java.net.BindException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.VersionConstans;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.utils.NetUtils;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaService;

/**
 * 
 * 所有server实现继承此类
 * 
 * @author seaky
 * @version @param <Req>
 * @version @param <Rsp>
 * @since 1.0.0
 */
public abstract class AbstractServer<Req, Rsp> implements Server<Req, Rsp> {

  private volatile boolean isStart;

  private RegisterationService registerationService;

  protected ServerConfig config;

  private ConcurrentHashMap<String, JavaService> allservices =
      new ConcurrentHashMap<String, JavaService>();

  private ConcurrentHashMap<String, Long> serviceRegistTimes = new ConcurrentHashMap<>();

  // 服务的拦截器
  private ConcurrentHashMap<String, List<ServiceInterceptor>> allservicesInterceptors =
      new ConcurrentHashMap<String, List<ServiceInterceptor>>();

  // 请求分发
  private RequestDispatcher<Req, Rsp> dispatcher;

  private ServerInterceptorSupportService<Req, Rsp> interceptorSupportService;

  private ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);

  private long startTime = 0L;

  protected AbstractServer(ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    if (protocolExtensionFactory == null)
      throw new IllegalArgumentException("protocolExtensionFactory can not be null");
    this.protocolExtensionFactory = protocolExtensionFactory;
    this.dispatcher = new RequestDispatcher<Req, Rsp>(this);
    this.interceptorSupportService =
        new ServerInterceptorSupportService<Req, Rsp>(protocolExtensionFactory);
  }

  /**
   * 不能重复绑定
   */
  @Override
  public synchronized void export(JavaService service, EndpointConfig serviceConfig) {
    if (!isRunning()) {
      throw new RuntimeException("server has not running");
    }
    if (serviceConfig == null)
      throw new RuntimeException("service config can not be null");

    String app = serviceConfig.get(ConfigConstans.PROVIDER_APP);
    if (app == null || !app.matches(Constants.APP_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "app name " + app + " is not match " + Constants.APP_NAME_ALLOW_REG);
    }

    String serviceName = serviceConfig.get(ConfigConstans.PROVIDER_NAME);
    if (serviceName == null || !serviceName.matches(Constants.SERVICE_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service name " + serviceName + " is not match " + Constants.SERVICE_NAME_ALLOW_REG);
    }
    String version = serviceConfig.get(ConfigConstans.PROVIDER_VERSION);
    if (version == null || !version.matches(Constants.VERSION_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service version " + version + " is not match " + Constants.VERSION_NAME_ALLOW_REG);
    }
    Utils.checkVersionFormat(version);

    String group = serviceConfig.get(ConfigConstans.PROVIDER_GROUP);

    if (group == null || !group.matches(Constants.GROUP_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service group " + group + " is not match " + Constants.GROUP_NAME_ALLOW_REG);
    }
    if (service == null)
      throw new IllegalArgumentException("service instance can not be null");

    String serviceKey = Utils.generateKey(serviceName, app, version, group);
    JavaService oldService = allservices.putIfAbsent(serviceKey, service);
    if (oldService != null) {
      throw new RuntimeException("server already bind service [service name: " + serviceName
          + ",app: " + app + ",version: " + version + ",group: " + group + "]");
    }

    // 设置拦截器
    allservicesInterceptors.putIfAbsent(serviceKey, Utils.extractByProcessPhase(
        serviceConfig.get(ConfigConstans.PROVIDER_INTERCEPTORS), ProcessPhase.SERVER_CALL_SERVICE));
    ServiceProviderDescriptor sd = new ServiceProviderDescriptor();
    EndpointConfig copyOfConfig = serviceConfig.deepCopy();
    sd.setConfig(copyOfConfig);
    sd.setHost(config.getHost());
    sd.setPort(config.getPort());
    sd.setProtocol(protocolExtensionFactory.protocolName());
    long curtime = System.currentTimeMillis();
    sd.setRegistTime(curtime);
    sd.setPid(Utils.getCurrentVmPid());
    sd.setFrameworkVersion(VersionConstans.VERSION);
    serviceRegistTimes.put(serviceKey, curtime);
    try {
      registerationService.registServiceProvider(sd);
    } catch (Exception e) {
      allservices.remove(serviceKey);
      throw new RuntimeException(e);
    }
    logger.info("export service {}:{}:{}:{} on {}:{} success ", serviceName, app, version, group,
        config.getHost(), config.getPort());
  }

  @Override
  public JavaService findService(String serviceName, String app, String version, String group) {
    if (!isRunning())
      throw new RuntimeException("server has closed");
    String serviceKey = Utils.generateKey(serviceName, app, version, group);
    return allservices.get(serviceKey);
  }

  private synchronized void start(RegisterationService registerationService, ServerConfig config,
      boolean isAutoChooseAddr, boolean isAutoChoosePort) {
    try {
      String host = config.getHost();
      InetAddress addr = null;
      int port = config.getPort();
      if (isAutoChooseAddr) {
        // 自动寻找ip和端口
        addr = NetUtils.getLocalAddress();
        host = addr.getHostAddress();
        logger.info("server auto choose addr {}", addr.getHostAddress());
      }
      if (isAutoChoosePort) {
        port = NetUtils.getAvailablePort(config.getStartScanPort(), config.getEndScanPort());
        config.setPort(port);
        logger.info("server auto choose port {}", port);
      }

      config.setHost(host);
      config.setPort(port);
      // 启动公共资源
      ServerResourceManager.start();
      this.config = config;
      doStart(config);
      isStart = true;
      this.registerationService = registerationService;
      logger.info("start server on addr {}:{}", config.getHost(), config.getPort());
    } catch (BindException e) {
      if (isAutoChoosePort) {
        // 有冲突
        start(registerationService, config, isAutoChooseAddr, isAutoChoosePort);
      } else {
        close();
        Utils.throwException(e);
      }
    } catch (InterruptedException e) {
      close();
      Thread.currentThread().interrupt();
    } catch (RuntimeException e) {
      close();
      throw e;
    } catch (Exception e) {
      close();
      Utils.throwException(e);
    }

  }

  @Override
  public synchronized void start(RegisterationService registerationService, ServerConfig config) {
    if (registerationService == null)
      throw new IllegalArgumentException("regist service  can not be null");
    if (config == null)
      throw new IllegalArgumentException("server config  can not be null");
    if (isRunning())
      throw new RuntimeException(
          "server is running at host " + this.config.getHost() + ",port " + this.config.getPort());
    boolean isAutoChooseAddr = false;
    if (StringUtils.isBlank(config.getHost())) {
      isAutoChooseAddr = true;
    }
    boolean isAutoChoosePort = false;

    if (config.getPort() <= 0) {
      isAutoChoosePort = true;
    }
    start(registerationService, config, isAutoChooseAddr, isAutoChoosePort);
    setStartTime(System.currentTimeMillis());
  }

  protected abstract void doStart(ServerConfig config) throws Exception;

  public synchronized void close() {
    releaseResource();
    unboundAllService();
    isStart = false;
    logger.info("close server,addr is {}:{}", config.getHost(), config.getPort());
  }

  protected abstract void releaseResource();

  private void unboundAllService() {
    for (String name : allservices.keySet()) {
      String[] attrs = name.split(Constants.TILDE_LINE);
      ServiceProviderDescriptor sd = new ServiceProviderDescriptor();
      sd.setHost(config.getHost());
      sd.setPort(config.getPort());
      sd.setName(attrs[0]);
      sd.setApp(attrs[1]);
      sd.setVersion(attrs[2]);
      sd.setGroup(attrs[3]);
      sd.setProtocol(protocolExtensionFactory.protocolName());
      sd.setPid(Utils.getCurrentVmPid());
      try {
        sd = registerationService.findServiceProvider(sd.getApp(), sd.getName(), sd.getVersion(),
            sd.getGroup(), sd.getProtocol(), sd.getHost(), sd.getPort(), Utils.getCurrentVmPid(),
            serviceRegistTimes.get(name));
        registerationService.unregistServiceProvider(sd);
        logger.info("unbound service {}:{}:{}:{},on {}:{} ", attrs[0], attrs[1], attrs[2], attrs[3],
            config.getHost(), config.getPort());
      } catch (Exception e) {
        logger.error("unregist service {}  error ", sd.toString(), e);
      }
    }
    allservices.clear();
  }

  public RegisterationService getRegisterationService() {
    return registerationService;
  }

  public boolean isRunning() {
    return isStart;
  }

  public ServerConfig getServerConfig() {
    return config;
  }

  /**
   * @return the processor
   */
  public RequestDispatcher<Req, Rsp> getRequestDispatcher() {
    return dispatcher;
  }

  /**
   * @return the protocolExtensionFactory
   */
  public ProtocolExtensionFactory<Req, Rsp> getProtocolExtensionFactory() {
    return protocolExtensionFactory;
  }

  /**
   * @return the interceptorSupportService
   */
  public ServerInterceptorSupportService<Req, Rsp> getInterceptorSupportService() {
    return interceptorSupportService;
  }

  public EndpointConfig getServiceConfig(String app, String serviceName, String version,
      String group) {
    String serviceKey = Utils.generateKey(serviceName, app, version, group);
    return registerationService.findServiceProvider(app, serviceName, version, group,
        protocolExtensionFactory.protocolName(), config.getHost(), config.getPort(),
        Utils.getCurrentVmPid(), serviceRegistTimes.get(serviceKey)).getConfig();
  }

  ServiceProviderDescriptor getServiceDescriptor(String app, String serviceName, String version,
      String group) {
    String serviceKey = Utils.generateKey(serviceName, app, version, group);
    return registerationService.findServiceProvider(app, serviceName, version, group,
        protocolExtensionFactory.protocolName(), config.getHost(), config.getPort(),
        Utils.getCurrentVmPid(), serviceRegistTimes.get(serviceKey));
  }

  public List<ServiceInterceptor> getServiceInterceptor(String serviceName, String app,
      String version, String group) {
    String key = Utils.generateKey(serviceName, app, version, group);

    return allservicesInterceptors.get(key);
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

}
