package com.seaky.hamster.core.service;

import java.lang.reflect.Method;

/**
 * 
 * java方法来实现java服务
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
public class JavaMethodServiceImpl implements JavaService {

  private Method method;

  private Object objImpl;

  private final Class<?>[] paramTypes;

  private final Class<?> returnType;

  public JavaMethodServiceImpl(Object implObj, Method method) {

    this.objImpl = implObj;
    this.method = method;
    this.paramTypes = method.getParameterTypes();
    this.returnType = method.getReturnType();
  }

  @Override
  public Object process(Object[] request) throws Exception {
    return method.invoke(objImpl, request);
  }

  /**
   * @return the paramTypes
   */
  public final Class<?>[] paramTypes() {
    return paramTypes;
  }

  public final Class<?> returnType() {
    return returnType;
  }

}
