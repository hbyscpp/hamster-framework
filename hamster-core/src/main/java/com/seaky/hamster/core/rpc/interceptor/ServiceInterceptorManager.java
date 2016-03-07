package com.seaky.hamster.core.rpc.interceptor;

import java.util.Set;

import org.reflections.Reflections;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import com.seaky.hamster.core.objectfactory.ObjectCreatorFactory;
import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;

public class ServiceInterceptorManager {


  private static ConcurrentHashMap<String, ServiceInterceptor> allInterceptors =
      new ConcurrentHashMap<String, ServiceInterceptor>();

  private static ConcurrentHashMap<String, Class<?>> allInterceptorsClass =
      new ConcurrentHashMap<String, Class<?>>();
  static {
    Reflections r = new Reflections(new Object[0]);
    Set<Class<?>> clss = r.getTypesAnnotatedWith(ServiceInterceptorAnnotation.class);

    if (clss != null) {
      for (Class<?> cls : clss) {

        if (cls.isAssignableFrom(ServiceInterceptor.class))
          throw new RuntimeException(
              cls.getName() + "must inherit from " + ServiceInterceptor.class.getName());
        ServiceInterceptorAnnotation anno = cls.getAnnotation(ServiceInterceptorAnnotation.class);
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
