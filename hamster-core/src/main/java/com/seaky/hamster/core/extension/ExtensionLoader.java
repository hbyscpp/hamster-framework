package com.seaky.hamster.core.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.objectfactory.ObjectCreatorFactory;

public class ExtensionLoader<T> {

  private static Reflections reflections = null;

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
    reflections = new Reflections(packageNames);

  }

  private String defaultName;
  private static ConcurrentHashMap<Class<?>, ExtensionLoader<?>> allExt =
      new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();
  private ConcurrentHashMap<String, T> allExtInstances = new ConcurrentHashMap<String, T>();

  private Collection<T> allIns = null;

  private ExtensionLoader(Class<T> cls) {

    if (cls == null)
      throw new IllegalArgumentException("Extension type == null");
    if (!cls.isInterface()) {
      throw new IllegalArgumentException("Extension type(" + cls + ") is not interface!");
    }
    if (!cls.isAnnotationPresent(SPI.class)) {
      throw new IllegalArgumentException("Extension type(" + cls
          + ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
    }
    SPI spi = cls.getAnnotation(SPI.class);

    defaultName = spi.value();
    if (StringUtils.isBlank(defaultName))
      throw new RuntimeException(cls + " spi annotation value can not be blank!");
    Set<Class<? extends T>> allCls = reflections.getSubTypesOf(cls);
    if (allCls != null) {
      for (Class<? extends T> c : allCls) {
        SPI s = c.getAnnotation(SPI.class);
        if (s != null) {
          String key = s.value();
          if (StringUtils.isBlank(key))
            throw new RuntimeException(c + " spi annotation value can not be blank!");
          T t = ObjectCreatorFactory.create(c, spi.creator());
          T oldt = allExtInstances.putIfAbsent(key, t);
          if (oldt != null)
            throw new RuntimeException(c + "," + oldt.getClass() + " has same key " + key);

        }
      }
    }
    allIns = Collections.unmodifiableCollection(allExtInstances.values());
  }

  @SuppressWarnings("unchecked")
  public static <T> ExtensionLoader<T> getExtensionLoaders(Class<T> cls) {
    ExtensionLoader<T> et = (ExtensionLoader<T>) allExt.get(cls);
    if (et == null) {
      et = new ExtensionLoader<T>(cls);
      allExt.putIfAbsent(cls, et);
    }
    return (ExtensionLoader<T>) allExt.get(cls);
  }

  public T findExtension(String name) {
    return allExtInstances.get(name);
  }

  public T findDefaultExtension() {
    return allExtInstances.get(defaultName);
  }

  public Collection<T> findAllExtension() {
    return allIns;
  }
}
