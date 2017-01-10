package com.seaky.hamster.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.seaky.hamster.core.ClientHelper;
import com.seaky.hamster.core.ServerHelper;
import com.seaky.hamster.core.objectfactory.ObjectCreatorFactory;
import com.seaky.hamster.core.rpc.client.Client;
import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.client.ClientResourceManager;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.registeration.EtcdRegisterationService;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.server.Server;
import com.seaky.hamster.core.rpc.server.ServerConfig;
import com.seaky.hamster.core.rpc.server.ServerResourceManager;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

public abstract class HamsterSpringSupport implements ApplicationContextAware {

  private ApplicationContext context;

  private RegisterationService service;

  private ConcurrentHashMap<String, Server<?, ?>> allServers =
      new ConcurrentHashMap<String, Server<?, ?>>();

  private ConcurrentHashMap<String, Client<?, ?>> allClients =
      new ConcurrentHashMap<String, Client<?, ?>>();


  public void createRegisterationService(String basePath, String urls) {
    this.service = new EtcdRegisterationService(basePath, urls);
  }

  public void createRegisterationService(String urls) {
    this.service = new EtcdRegisterationService("hamster", urls);
  }

  public Server<?, ?> createServer(String id, String protocolName, ServerConfig config) {

    if (allServers.get(id) != null)
      throw new RuntimeException("server " + id + " exists");
    Server<?, ?> server = ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION
        .findExtension(protocolName).createServer();

    server.start(service, config);
    allServers.putIfAbsent(id, server);
    return server;
  }

  public Server<?, ?> createServer(String protocolName, ServerConfig config) {
    return createServer("default", protocolName, config);
  }

  public Client<?, ?> createClient(String protocolName, ClientConfig config) {
    return createClient("default", protocolName, config);
  }

  public Client<?, ?> createClient(String id, String name, ClientConfig config) {
    if (allClients.get(id) != null)
      throw new RuntimeException("client " + id + " exists");
    Client<?, ?> cc =
        ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension(name).createClient();
    ClientConfig conf = new ClientConfig();
    cc.connect(service, conf);
    allClients.putIfAbsent(id, cc);
    return cc;
  }

  public <T> void export(Class<T> cls, ClassExportConfig config) {

    T obj = null;
    if (config.getBeanId() != null) {
      obj = context.getBean(config.getBeanId(), cls);
    } else {
      obj = context.getBean(cls);
    }
    EndpointConfig ec = createEndPonintConfig(config);
    Map<String, EndpointConfig> mconfigs = createExportMethodConfig(config.getMethodConfigs());
    ServerHelper.exportInterface(allServers.get(config.getServerId()), cls, obj, ec, mconfigs);
  }

  private static Map<String, EndpointConfig> createExportMethodConfig(
      Map<String, ServiceConfig> sconfigs) {
    Map<String, EndpointConfig> configs = new HashMap<String, EndpointConfig>();
    if (sconfigs != null) {
      for (Entry<String, ServiceConfig> entry : sconfigs.entrySet()) {
        configs.put(entry.getKey(), createEndPonintConfig(entry.getValue()));
      }
    }
    return configs;

  }

  private static Map<String, EndpointConfig> createReferenceMethodConfig(
      Map<String, ReferenceConfig> sconfigs) {
    Map<String, EndpointConfig> configs = new HashMap<String, EndpointConfig>();
    if (sconfigs != null) {
      for (Entry<String, ReferenceConfig> entry : sconfigs.entrySet()) {
        configs.put(entry.getKey(), createEndPonintConfig(entry.getValue()));
      }
    }
    return configs;

  }

  private static EndpointConfig createEndPonintConfig(ServiceConfig config) {
    EndpointConfig ec = new EndpointConfig();

    addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_APP, config.getApp(), true));
    addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_GROUP, config.getGroup(), true));
    addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_VERSION, config.getVersion(), true));
    addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_MAX_CONCURRENT,
        String.valueOf(config.getMaxConcurrent()), false));
    addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_EXCEPTION_CONVERTOR,
        config.getExceptionConvertor(), false));
    addConfigItem(ec,
        new ConfigItem(ConfigConstans.PROVIDER_INTERCEPTORS, config.getInterceptors(), true));
    if (config.isHidden() == null) {
      if (System.getProperty("hamster.provider.hidden") != null) {
        addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_HIDDERN,
            System.getProperty("hamster.provider.hidden"), false));
      } else {
        addConfigItem(ec,
            new ConfigItem(ConfigConstans.PROVIDER_HIDDERN, String.valueOf(false), false));
      }
    } else {
      addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_HIDDERN,
          String.valueOf(config.isHidden()), false));
    }
    addConfigItem(ec, new ConfigItem(ConfigConstans.PROVIDER_FORCE_ACCESS,
        String.valueOf(config.isForceAccess()), false));
    return ec;
  }

  private static EndpointConfig createEndPonintConfig(ReferenceConfig config) {
    EndpointConfig ec = new EndpointConfig();
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_APP, config.getApp(), true));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_GROUP, config.getGroup(), true));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_VERSION, config.getVersion(), true));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_MAX_CONCURRENT,
        String.valueOf(config.getMaxConcurrent()), false));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_READ_TIMEOUT,
        String.valueOf(config.getReadtimeout()), false));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_EXCEPTION_CONVERTOR,
        config.getExceptionConvertor(), false));
    // 断路器
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_SUCCESS_NUMBER,
        String.valueOf(config.getCircuitbreakerCloseSuccessNumber()), false));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_TOTAL_NUMBER,
        String.valueOf(config.getCircuitbreakerCloseTotalNumber()), false));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_FAIL_NUMBER,
        String.valueOf(config.getCircuitbreakerOpenFailNumber()), false));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_TOTAL_NUMBER,
        String.valueOf(config.getCircuitbreakerOpenTotalNumber()), false));
    addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_CIRCUITBREAKER_HALFOPEN_DELAY,
        String.valueOf(config.getCircuitbreakerHalfopenDelay()), false));
    if (StringUtils.isNotBlank(config.getProviderAddresses()))
      addConfigItem(ec, new ConfigItem(ConfigConstans.REFERENCE_SERVICE_PROVIDER_ADDRESSES,
          config.getProviderAddresses(), false));
    if (config.getInterceptors() != null) {
      addConfigItem(ec,
          new ConfigItem(ConfigConstans.PROVIDER_INTERCEPTORS, config.getInterceptors(), true));
    }
    return ec;
  }

  private static void addConfigItem(EndpointConfig ec, ConfigItem item) {
    if (item.getValue() == null)
      return;
    ec.addConfigItem(item);
  }

  public <T> T referenceService(Class<T> cls, ClassReferenceConfig config) {

    Map<String, EndpointConfig> mconfigs = createReferenceMethodConfig(config.getMethodConfigs());
    return ClientHelper.referInterface(allClients.get(config.getClientId()), cls,
        createEndPonintConfig(config), mconfigs);
  }

  public <T, V> V referenceAsynService(Class<T> cls, Class<V> asynClass,
      ClassReferenceConfig config) {
    Map<String, EndpointConfig> mconfigs = createReferenceMethodConfig(config.getMethodConfigs());
    return ClientHelper.referAsynInterface(allClients.get(config.getClientId()), cls, asynClass,
        createEndPonintConfig(config), mconfigs);
  }

  public <T, V> V referenceReactiveService(Class<T> cls, Class<V> reactiveClass,
      ClassReferenceConfig config) {
    Map<String, EndpointConfig> mconfigs = createReferenceMethodConfig(config.getMethodConfigs());
    return ClientHelper.referReactiveInterface(allClients.get(config.getClientId()), cls,
        reactiveClass, createEndPonintConfig(config), mconfigs);
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

  public ApplicationContext getContext() {
    return context;
  }

  public void setContext(ApplicationContext context) {
    this.context = context;
  }

  @PostConstruct
  public void afterPropertiesSet() throws Exception {
    initSpringObjectCreator();
    initRegisterationService();
    initServer();
    initClient();
  }

  @PreDestroy
  public void destory() {

    for (Client<?, ?> client : allClients.values()) {
      client.close();
    }
    ClientResourceManager.stop();

    for (Server<?, ?> server : allServers.values()) {
      server.close();
    }
    ServerResourceManager.stop();
    if (service != null)
      service.close();

  }

  public void initSpringObjectCreator() {
    SpringObjectCreator creator = new SpringObjectCreator();
    creator.setApplicationContext(context);
    ObjectCreatorFactory.registerObjectCreator("spring", creator);
  }

  public abstract void initRegisterationService();

  public abstract void initServer();

  public abstract void initClient();

}
