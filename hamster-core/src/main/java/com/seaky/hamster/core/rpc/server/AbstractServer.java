package com.seaky.hamster.core.rpc.server;

import java.net.BindException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private ConcurrentHashMap<String, List<ServiceInterceptor>> allservicesInterceptors =
      new ConcurrentHashMap<String, List<ServiceInterceptor>>();

  private RequestDispatcher<Req, Rsp> dispatcher;

  private ServerInterceptorSupportService<Req, Rsp> interceptorSupportService;

  private ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);

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
          "app name contains special char,app name must compose of [0-9 _ . $ a-z A-Z -]");
    }

    String serviceName = serviceConfig.get(ConfigConstans.PROVIDER_NAME);
    if (serviceName == null || !serviceName.matches(Constants.SERVICE_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service name contains special char,service name must compose of [0-9 _ . $ a-z A-Z]");
    }
    String version = serviceConfig.get(ConfigConstans.PROVIDER_VERSION);
    if (version == null || !version.matches(Constants.VERSION_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service version contains special char,service version must compose of [0-9 _ . $ a-z A-Z]");
    }
    Utils.checkVersionFormat(version);

    String group = serviceConfig.get(ConfigConstans.PROVIDER_GROUP);

    if (group == null || !group.matches(Constants.GROUP_NAME_ALLOW_REG)) {
      throw new IllegalArgumentException(
          "service group contains special char,service version must compose of [0-9 _ . $ a-z A-Z]");
    }
    if (service == null)
      throw new IllegalArgumentException("service instance can not be null");

    String serviceKey = Utils.generateKey(serviceName, app, version, group);
    JavaService oldService = allservices.putIfAbsent(serviceKey, service);
    if (oldService != null) {
      throw new RuntimeException("server already bind service [service name: " + serviceName
          + ",app: " + app + ",version: " + version + ",group: " + group + "]");
    }

    allservicesInterceptors.putIfAbsent(serviceKey, Utils.extractByProcessPhase(
        serviceConfig.get(ConfigConstans.PROVIDER_INTERCEPTORS), ProcessPhase.SERVER_CALL_SERVICE));
    ServiceProviderDescriptor sd = new ServiceProviderDescriptor();
    EndpointConfig copyOfConfig = serviceConfig.deepCopy();
    sd.setConfig(copyOfConfig);
    sd.setHost(config.getHost());
    sd.setPort(config.getPort());
    sd.setProtocol(protocolExtensionFactory.protocolName());
    sd.setRegistTime(System.currentTimeMillis());
    sd.setPid(Utils.getCurrentVmPid());
    try {
      registerationService.registService(sd);
    } catch (Exception e) {
      allservices.remove(serviceKey);
      throw new RuntimeException(e);
    }
    logger.info("export service {}:{}:{}:{} on {}:{} ", serviceName, app, version, group,
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
    if (registerationService == null || config == null)
      throw new IllegalArgumentException("regist service or server config  can not be null");
    if (isRunning())
      throw new RuntimeException(
          "server is running at host " + this.config.getHost() + ",port " + this.config.getPort());
    boolean isAutoChooseAddr = false;
    if (NetUtils.isInvalidLocalHost(config.getHost())) {
      isAutoChooseAddr = true;
    }
    boolean isAutoChoosePort = false;

    if (config.getPort() <= 0) {
      isAutoChoosePort = true;
    }

    start(registerationService, config, isAutoChooseAddr, isAutoChoosePort);

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
        sd = registerationService.findService(sd.getApp(), sd.getName(), sd.getVersion(),
            sd.getGroup(), sd.getProtocol(), sd.getHost(), sd.getPort());
        registerationService.unregistService(sd);
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

    return registerationService.findService(app, serviceName, version, group,
        protocolExtensionFactory.protocolName(), config.getHost(), config.getPort()).getConfig();
  }

  public List<ServiceInterceptor> getServiceInterceptor(String serviceName, String app,
      String version, String group) {
    String key = Utils.generateKey(serviceName, app, version, group);

    return allservicesInterceptors.get(key);
  }

}
