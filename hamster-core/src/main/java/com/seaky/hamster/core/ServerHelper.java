package com.seaky.hamster.core;


import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.server.Server;
import com.seaky.hamster.core.rpc.server.ServerConfig;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaMethodServiceImpl;
import com.seaky.hamster.core.service.JavaService;

public final class ServerHelper {

  private ServerHelper() {

  }

  /**
   * 
   * 创建一个Server，同时启动server
   * 
   * @param name 协议的名字
   * @param registerationService 注册中心
   * @param config 服务的配置
   * @return Server<?,?> server实例
   */
  public static Server<?, ?> createServer(String name, RegisterationService registerationService,
      ServerConfig config) {
    ProtocolExtensionFactory<?, ?> factory =
        ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension(name);
    if (factory == null)
      throw new RuntimeException("cannot find ProtocolExtensionFactory " + name);
    Server<?, ?> server = factory.createServer();
    server.start(registerationService, config);
    return server;
  }

  public static <Req, Rsp> void exportInterface(Server<Req, Rsp> server, Class<?> cls,
      Object implObj, EndpointConfig commonConfig) {
    exportInterface(server, cls, implObj, commonConfig, null);
  }

  /**
   * 向server中注册服务，接口中每个方法代表一个服务，<br/>
   * 接口不能有重载的方法
   * 
   * @param server 服务
   * @param cls 服务接口
   * @param implObj 服务实现
   * @param commonConfig 服务的公共配置
   * @param configs 接口方法特定的配置
   */
  public static <Req, Rsp> void exportInterface(Server<Req, Rsp> server, Class<?> cls,
      Object implObj, EndpointConfig commonConfig, Map<String, EndpointConfig> configs) {
    if (cls == null)
      throw new RuntimeException("interface can not be null ");
    if (!cls.isInterface())
      throw new RuntimeException(cls.getName() + " must be interface");
    if (implObj == null)
      throw new RuntimeException("interface implementation object can not be null ");
    if (server == null)
      throw new RuntimeException("server can not be null ");

    if (!cls.isAssignableFrom(implObj.getClass()))
      throw new RuntimeException(
          implObj.getClass().getName() + " must be implement interface " + cls.getName());
    Method[] methods = cls.getMethods();
    Set<String> methodNames = new HashSet<String>();
    for (Method m : methods) {
      // 判断重载
      if (methodNames.contains(m.getName()))
        throw new RuntimeException(
            "interface " + cls.getName() + " contain overload method " + m.getName());
      methodNames.add(m.getName());
    }

    if (configs != null) {

      for (String key : configs.keySet()) {

        if (!methodNames.contains(key))
          throw new RuntimeException("config error,can not found " + key + " method");
      }

    }
    for (Method m : methods) {
      methodNames.add(m.getName());
      JavaService cs = new JavaMethodServiceImpl(implObj, m);
      String serviceName = cls.getName() + "_" + m.getName();
      EndpointConfig sc = null;
      if (configs != null) {
        sc = configs.get(m.getName());
      }
      if (sc == null) {
        sc = new EndpointConfig();
      }
      sc.mergeServiceConfig(commonConfig);
      sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_NAME, serviceName));
      sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_RETURN, m.getReturnType().getName()));
      Class<?>[] params = m.getParameterTypes();
      if (params != null) {
        String[] paramNames = new String[params.length];
        for (int i = 0; i < params.length; ++i) {
          paramNames[i] = params[i].getName();
        }
        sc.addConfigItem(
            new ConfigItem(ConfigConstans.PROVIDER_PARAMS, Utils.paramsToString(paramNames)));
      }

      server.export(cs, sc);
    }
  }
}
