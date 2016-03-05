package com.seaky.hamster.core.objectfactory;

import com.seaky.hamster.core.rpc.utils.Utils;

public class DefaultObjectCreator implements ObjectCreator {

  @Override
  public <T> T create(Class<T> cls) {
    try {
      return cls.newInstance();
    } catch (Exception e) {
      Utils.throwException(e);
    }

    return null;
  }

}
