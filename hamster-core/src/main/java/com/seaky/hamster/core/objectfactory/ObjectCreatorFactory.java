package com.seaky.hamster.core.objectfactory;

import java.util.concurrent.ConcurrentHashMap;

public class ObjectCreatorFactory {


  private static ConcurrentHashMap<String, ObjectCreator> objectCreatorMaps =
      new ConcurrentHashMap<String, ObjectCreator>();
  static {
    registerObjectCreator("default", new DefaultObjectCreator());
  }

  public static <T> T create(Class<T> cls, String name) {

    ObjectCreator oc = objectCreatorMaps.get(name);
    if (oc == null)
      throw new RuntimeException(name + " object creator not found");
    return oc.create(cls);
  }

  public static void registerObjectCreator(String name, ObjectCreator objectCreator) {
    if (objectCreatorMaps.putIfAbsent(name, objectCreator) != null)
      throw new RuntimeException(name + " object creator has register");
  }

}
