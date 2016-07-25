package com.seaky.hamster.core.rpc.interceptor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import com.seaky.hamster.core.objectfactory.ObjectCreatorFactory;
import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.Constants;

public class ServiceInterceptorManager {

  private static ConcurrentHashMap<String, ServiceInterceptor> allInterceptors =
      new ConcurrentHashMap<String, ServiceInterceptor>();

  private static ConcurrentHashMap<String, Class<?>> allInterceptorsClass =
      new ConcurrentHashMap<String, Class<?>>();
  static {
    Package[] packages = Package.getPackages();
    Set<String> packageNames = new HashSet<>();

    for (int i = 0; i < packages.length; ++i) {
      int index = packages[i].getName().indexOf(".");
      if (index == -1) {
        packageNames.add(packages[i].getName());
      } else {
        packageNames.add(packages[i].getName().substring(0, index));
      }
    }
    Reflections r = new Reflections(packageNames);


    Set<Class<?>> clss = r.getTypesAnnotatedWith(ServiceInterceptorAnnotation.class);

    if (clss != null) {
      for (Class<?> cls : clss) {

        if (cls.isAssignableFrom(ServiceInterceptor.class))
          throw new RuntimeException(
              cls.getName() + "must inherit from " + ServiceInterceptor.class.getName());
        ServiceInterceptorAnnotation anno = cls.getAnnotation(ServiceInterceptorAnnotation.class);
        if (!anno.name().matches(Constants.INTERCEPTOR_NAME_ALLOW_REG))
          throw new RuntimeException(
              "interceptor name contains special char,service version must compose of [0-9a-zA-Z],but "
                  + cls.getName() + " name is " + anno.name());
        if (allInterceptorsClass.putIfAbsent(anno.name(), cls) != null) {
          throw new RuntimeException(anno.name() + " service interceptor has regist in "
              + allInterceptorsClass.get(anno.name()).getName());
        }
      }
    }
  }

  public static ServiceInterceptor createServiceInterceptor(String name) {

    ServiceInterceptor interceptor = allInterceptors.get(name);
    if (interceptor != null) {

      return interceptor;
    }
    Class<?> cls = allInterceptorsClass.get(name);
    if (cls == null)
      throw new RuntimeException("not found " + name + " service interceptor");

    ServiceInterceptorAnnotation anno = cls.getAnnotation(ServiceInterceptorAnnotation.class);
    Object obj = ObjectCreatorFactory.create(cls, anno.creator());

    ServiceInterceptor si = allInterceptors.putIfAbsent(name, (ServiceInterceptor) obj);
    if (si == null)
      return (ServiceInterceptor) obj;
    return si;
  }
}
