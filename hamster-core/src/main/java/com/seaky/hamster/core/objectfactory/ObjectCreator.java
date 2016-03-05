package com.seaky.hamster.core.objectfactory;

public interface ObjectCreator {

  <T> T create(Class<T> cls);
}
