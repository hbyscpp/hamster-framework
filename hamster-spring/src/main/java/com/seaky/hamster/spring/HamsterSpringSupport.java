package com.seaky.hamster.spring;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.seaky.hamster.core.ClientHelper;
import com.seaky.hamster.core.ServerHelper;
import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.client.Client;
import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.client.ClientResourceManager;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.EtcdRegisterationService;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.server.Server;
import com.seaky.hamster.core.rpc.server.ServerConfig;
import com.seaky.hamster.core.rpc.server.ServerResourceManager;

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
    Server<?, ?> server = ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class)
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
    Client<?, ?> cc = ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class)
        .findExtension(name).createClient();
    ClientConfig conf = new ClientConfig();
    cc.connect(service, conf);
    allClients.putIfAbsent(id, cc);
    return cc;
  }

  public <T> void export(Class<T> cls, ClassExportConfig config) {

    T obj = null;
    if (config.getBeanId() != null) {
      obj = (T) context.getBean(config.getBeanId());
    } else {
      obj = context.getBean(cls);
    }
    EndpointConfig ec = createEndPonintConfig(config);

    ServerHelper.exportInterface(allServers.get(config.getServerId()), cls, obj, ec, null);

  }

  private static EndpointConfig createEndPonintConfig(ClassExportConfig config) {
    EndpointConfig ec = new EndpointConfig();

    ec.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_APP, config.getApp(), true));
    ec.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_GROUP, config.getGroup(), true));
    ec.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_VERSION, config.getVersion(), true));
    ec.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_MAX_CONCURRENT,
        String.valueOf(config.getMaxConcurrent()), false));

    return ec;
  }

  private static EndpointConfig createEndPonintConfig(ClassReferenceConfig config) {
    EndpointConfig ec = new EndpointConfig();

    ec.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_APP, config.getApp(), true));
    ec.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_GROUP, config.getGroup(), true));
    ec.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_VERSION, config.getVersion(), true));
    ec.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_MAX_CONCURRENT,
        String.valueOf(config.getMaxConcurrent()), false));
    ec.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_READ_TIMEOUT,
        String.valueOf(config.getReadtimeout()), false));


    return ec;
  }

  public <T> T referenceService(Class<T> cls, ClassReferenceConfig config) {
    return ClientHelper.referInterface(allClients.get(config.getClientId()), cls,
        createEndPonintConfig(config), null);
  }

  public <T, V> V referenceAsynService(Class<T> cls, Class<V> asynClass,
      ClassReferenceConfig config) {
    return ClientHelper.referAsynInterface(allClients.get(config.getClientId()), cls, asynClass,
        createEndPonintConfig(config), null);
  }

  public <T, V> V referenceReactiveService(Class<T> cls, Class<V> reactiveClass,
      ClassReferenceConfig config) {
    return ClientHelper.referReactiveInterface(allClients.get(config.getClientId()), cls,
        reactiveClass, createEndPonintConfig(config), null);
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

  public abstract void initRegisterationService();

  public abstract void initServer();

  public abstract void initClient();

}
