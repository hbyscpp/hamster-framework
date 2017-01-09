package com.seaky.hamster.core.rpc.common;

import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.trace.ClientCallExceptionTrace;

public class ServiceContextUtils {

  public static ProtocolRequestHeader getRequestHeader(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REQUEST_HEADER, ProtocolRequestHeader.class);
  }

  public static void setRequestHeader(ServiceContext sc, ProtocolRequestHeader header) {

    sc.setAttribute(ServiceContext.REQUEST_HEADER, header);
  }

  public static ProtocolRequestBody getRequestBody(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REQUEST_BODY, ProtocolRequestBody.class);
  }

  public static void setRequestBody(ServiceContext sc, ProtocolRequestBody body) {

    sc.setAttribute(ServiceContext.REQUEST_BODY, body);
  }

  public static ProtocolResponseHeader getResponseHeader(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.RESPONSE_HEADER, ProtocolResponseHeader.class);
  }

  public static void setResponseHeader(ServiceContext sc, ProtocolResponseHeader header) {

    sc.setAttribute(ServiceContext.RESPONSE_HEADER, header);
  }

  public static ProtocolResponseBody getResponseBody(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.RESPONSE_BODY, ProtocolResponseBody.class);
  }

  public static void setResponseBody(ServiceContext sc, ProtocolResponseBody body) {

    sc.setAttribute(ServiceContext.RESPONSE_BODY, body);
  }



  public static EndpointConfig getReferenceConfig(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REFERENCE_CONFIG, EndpointConfig.class);
  }

  public static void setReferenceConfig(ServiceContext sc, EndpointConfig config) {

    sc.setAttribute(ServiceContext.REFERENCE_CONFIG, config);
  }

  public static EndpointConfig getProviderConfig(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.PROVIDER_CONFIG, EndpointConfig.class);
  }

  public static String getServerHost(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.SERVER_HOST, String.class);
  }

  public static Integer getServerPort(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.SERVER_PORT, Integer.class);
  }

  public static String getClientHost(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.CLINET_HOST, String.class);
  }

  public static Integer getClientPort(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.CLINET_PORT, Integer.class);
  }



  public static ClientCallExceptionTrace getInterceptorExceptionTrace(ServiceContext sc) {
    return sc.getAttribute(ServiceContext.EXCEPTION_TRACE, ClientCallExceptionTrace.class);
  }


  // -------------------set


  public static void setProviderConfig(ServiceContext sc, EndpointConfig config) {

    sc.setAttribute(ServiceContext.PROVIDER_CONFIG, config);
  }

  public static void setServerHost(ServiceContext sc, String host) {

    sc.setAttribute(ServiceContext.SERVER_HOST, host);
  }

  public static void setServerPort(ServiceContext sc, int port) {

    sc.setAttribute(ServiceContext.SERVER_PORT, port);
  }

  public static void setClientHost(ServiceContext sc, String host) {

    sc.setAttribute(ServiceContext.CLINET_HOST, host);
  }

  public static void setClientPort(ServiceContext sc, int port) {

    sc.setAttribute(ServiceContext.CLINET_PORT, port);
  }


  public static void setInterceptorExceptionTrace(ServiceContext sc,
      ClientCallExceptionTrace trace) {
    sc.setAttribute(ServiceContext.EXCEPTION_TRACE, trace);
  }

}
