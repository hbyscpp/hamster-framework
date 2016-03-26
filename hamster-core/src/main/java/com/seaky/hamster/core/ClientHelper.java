package com.seaky.hamster.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.seaky.hamster.core.rpc.client.Client;
import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaReferenceService;

import rx.Observable;

public final class ClientHelper {

  private ClientHelper() {

  }

  public static Client<?, ?> createServer(String name, RegisterationService registerationService,
      ClientConfig config) {
    ProtocolExtensionFactory<?, ?> factory =
        ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension(name);
    if (factory == null)
      throw new RuntimeException("cannot find ProtocolExtensionFactory " + name);
    Client<?, ?> client = factory.createClient();
    client.connect(registerationService, config);
    return client;
  }

  public static <T> T referInterface(final Client<?, ?> client, final Class<T> cls,
      EndpointConfig config) {

    return referInterface(client, cls, config, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> T referInterface(final Client<?, ?> client, final Class<T> cls,
      EndpointConfig commonConfig, final Map<String, EndpointConfig> configs) {
    if (client == null)
      throw new RuntimeException("client can not be null");
    if (cls == null || !cls.isInterface())
      throw new RuntimeException("class must be interface");
    final Map<String, JavaReferenceService> methodServices =
        getReferInterfaceMap(client, cls, commonConfig, configs);
    T t = (T) Proxy.newProxyInstance(client.getClass().getClassLoader(), new Class<?>[] {cls},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            JavaReferenceService service = methodServices.get(method.getName());
            Object r = service.process(args);
            return r;
          }
        });
    return t;
  }

  public static <T, V> V referAsynInterface(final Client<?, ?> client, final Class<T> cls,
      final Class<V> asynCls, EndpointConfig commonConfig) {

    return referAsynInterface(client, cls, asynCls, commonConfig, null);
  }

  @SuppressWarnings("unchecked")
  public static <T, V> V referAsynInterface(final Client<?, ?> client, final Class<T> cls,
      final Class<V> asynCls, final EndpointConfig commonConfig,
      final Map<String, EndpointConfig> configs) {
    if (client == null)
      throw new RuntimeException("client can not be null");
    if (cls == null || !cls.isInterface())
      throw new RuntimeException("class must be interface");
    if (asynCls == null || !asynCls.isInterface())
      throw new RuntimeException("class must be interface");
    // 初始化JavaService
    if (cls.getMethods().length != asynCls.getMethods().length)
      throw new RuntimeException("asyn class is not match ");
    for (Method method : cls.getMethods()) {
      try {
        Method asynM = asynCls.getMethod(method.getName(), method.getParameterTypes());
        Class<?> type = asynM.getReturnType();
        if (type != Future.class) {
          throw new RuntimeException("asyn class is not match ");
        }
      } catch (SecurityException e) {
        throw new RuntimeException("asyn class is not match ");
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("asyn class is not match ");
      }
    }

    final Map<String, JavaReferenceService> methodServices =
        getReferInterfaceMap(client, cls, commonConfig, configs);
    V t = (V) Proxy.newProxyInstance(client.getClass().getClassLoader(), new Class<?>[] {asynCls},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            JavaReferenceService service = methodServices.get(method.getName());
            Object r = service.processAsyn(args);
            return r;
          }
        });
    return t;
  }

  public static <T, V> V referReactiveInterface(

      final Client<?, ?> client, final Class<T> cls, final Class<V> asynCls,
      final EndpointConfig config) {

    return referReactiveInterface(client, cls, asynCls, config, null);
  }

  @SuppressWarnings("unchecked")
  public static <T, V> V referReactiveInterface(

      final Client<?, ?> client, final Class<T> cls, final Class<V> asynCls,
      final EndpointConfig commonConfig, final Map<String, EndpointConfig> configs) {
    if (client == null)
      throw new RuntimeException("client can not be null");
    if (cls == null || !cls.isInterface())
      throw new RuntimeException("class must be interface");
    if (asynCls == null || !asynCls.isInterface())
      throw new RuntimeException("class must be interface");
    // 初始化JavaService
    if (cls.getMethods().length != asynCls.getMethods().length)
      throw new RuntimeException("asyn class is not match ");
    for (Method method : cls.getMethods()) {
      try {
        Method asynM = asynCls.getMethod(method.getName(), method.getParameterTypes());
        Class<?> type = asynM.getReturnType();
        if (type != Observable.class) {
          throw new RuntimeException("reactive class is not match ");
        }
      } catch (SecurityException e) {
        throw new RuntimeException("reactive class is not match ");
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("reactive class is not match ");
      }
    }

    final Map<String, JavaReferenceService> methodServices =
        getReferInterfaceMap(client, cls, commonConfig, configs);
    V t = (V) Proxy.newProxyInstance(client.getClass().getClassLoader(), new Class<?>[] {asynCls},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            JavaReferenceService service = methodServices.get(method.getName());
            Object r = service.processReactive(args);
            return r;
          }
        });
    return t;
  }

  public static <T> Map<String, JavaReferenceService> getReferInterfaceMap(
      final Client<?, ?> client, final Class<T> cls, final EndpointConfig commonConfig,
      final Map<String, EndpointConfig> configs) {
    if (client == null)
      throw new RuntimeException("client can not be null");
    if (cls == null || !cls.isInterface())
      throw new RuntimeException("class must be interface");
    Method[] methods = cls.getMethods();
    Set<String> methodNames = new HashSet<String>();
    for (Method m : methods) {
      // 判断重载
      if (methodNames.contains(m.getName()))
        throw new RuntimeException("client does not support overload method");
      methodNames.add(m.getName());
    }
    // 初始化JavaService
    final Map<String, JavaReferenceService> methodServices =
        new HashMap<String, JavaReferenceService>();
    for (Method method : cls.getMethods()) {
      EndpointConfig sc = configs == null ? null : configs.get(method.getName());
      if (sc == null) {
        sc = new EndpointConfig();
      }
      sc.mergeServiceConfig(commonConfig);
      String serviceName = cls.getName() + "_" + method.getName();
      sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_NAME, serviceName, true));
      sc.addConfigItem(
          new ConfigItem(ConfigConstans.REFERENCE_RETURN, method.getReturnType().getName(), true));
      Class<?>[] params = method.getParameterTypes();
      if (params != null) {
        String[] paramNames = new String[params.length];
        for (int i = 0; i < params.length; ++i) {
          paramNames[i] = params[i].getName();
        }
        sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_PARAMS,
            Utils.paramsToString(paramNames), true));
      }
      JavaReferenceService service = client.reference(sc);
      methodServices.put(method.getName(), service);
    }
    return methodServices;
  }
}
