/**
 * Copyright (c) @2016,重庆云途交通科技有限公司.版权所有
 */
package com.seaky.hamster.core.rpc.common;

import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 6, 2016
 */
public interface ServiceContext {

	//TODO 控制读写

  public static final String SERVICENAME = "__SERVICENAME__";

  public static final String APP = "__APP__";

  public static final String GROUP = "__GROUP__";

  public static final String VERSION = "__VERSION__";

  public static final String REFERENCE_APP = "__REFERENCE_APP__";

  public static final String REFERENCE_GROUP = "__REFERENCEG_ROUP__";

  public static final String REFERENCE_VERSION = "__REFERENCE_VERSION__";

  public static final String SERVER_HOST = "__SERVER_HOST__";

  public static final String SERVER_PORT = "__SERVER_PORT__";

  public static final String CLINET_HOST = "__CLIENT_HOST__";

  public static final String CLINET_PORT = "__CLIENT_PORT__";

  public static final String REQUEST_PARAMS = "__REQUEST_PARAMS__";

  public static final String REQUEST_ATTACHMENTS = "__REQUEST_ATTACHMENTS__";

  public static final String RESPONSE = "__RESPONSE__";

  public static final String PROVIDER_CONFIG = "__PROVIDER_CONFIG__";

  public static final String REFERENCE_CONFIG = "__REFERENCE_CONFIG__";

  public static final String EXCEPTION_TRACE = "__EXCEPTION_TRACE__";


  Object getAttribute(String key);

  void setAttribute(String key, Object obj);

  void removeAttribute(String key);

  <T> T getAttribute(String key, Class<T> cls);

  ProcessPhase processPhase();


}
