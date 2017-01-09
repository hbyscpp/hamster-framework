package com.seaky.hamster.core.rpc.common;

import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since 1.0 从哪个项目版本开始创建
 * @Date Mar 6, 2016
 */
public interface ServiceContext {


  public static final String SERVER_HOST = "__SERVER_HOST__";

  public static final String SERVER_PORT = "__SERVER_PORT__";

  public static final String CLINET_HOST = "__CLIENT_HOST__";

  public static final String CLINET_PORT = "__CLIENT_PORT__";



  public static final String PROVIDER_CONFIG = "__PROVIDER_CONFIG__";

  public static final String REFERENCE_CONFIG = "__REFERENCE_CONFIG__";

  public static final String EXCEPTION_TRACE = "__EXCEPTION_TRACE__";

  public static final String REQUEST_HEADER = "__REQUEST_HEADER__";

  public static final String REQUEST_BODY = "__REQUEST_BODY__";

  public static final String RESPONSE_HEADER = "__RESPONSE_HEADER__";

  public static final String RESPONSE_BODY = "__RESPONSE_BODY__";

  Object getAttribute(String key);

  void setAttribute(String key, Object obj);

  void removeAttribute(String key);

  <T> T getAttribute(String key, Class<T> cls);

  ProcessPhase processPhase();


}
