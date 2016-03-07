package com.seaky.hamster.core.rpc.common;

import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.Response;

public class ServiceContextUtils {


  public static String getServiceName(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.SERVICENAME, String.class);
  }

  public static String getApp(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.APP, String.class);
  }

  public static String getGroup(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.GROUP, String.class);
  }

  public static String getVersion(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.VERSION, String.class);
  }

  public static String getReferenceApp(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REFERENCE_APP, String.class);
  }

  public static String getReferenceGroup(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REFERENCE_GROUP, String.class);
  }

  public static String getReferenceVersion(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REFERENCE_VERSION, String.class);
  }

  public static EndpointConfig getReferenceConfig(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REFERENCE_CONFIG, EndpointConfig.class);
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

  public static Object[] getRequestParams(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REQUEST_PARAMS, Object[].class);
  }

  public static Attachments getRequestAttachments(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.REQUEST_ATTACHMENTS, Attachments.class);
  }

  public static Response getResponse(ServiceContext sc) {

    return sc.getAttribute(ServiceContext.RESPONSE, Response.class);
  }

  public static ProcessPhase getSerivePhase(ServiceContext sc) {
    return sc.getAttribute(ServiceContext.SERVICE_PHASE, ProcessPhase.class);
  }


  public static void setServiceName(ServiceContext sc, String serviceName) {

    sc.setAttribute(ServiceContext.SERVICENAME, serviceName);
  }

  public static void setApp(ServiceContext sc, String app) {

    sc.setAttribute(ServiceContext.APP, app);
  }

  public static void setGroup(ServiceContext sc, String group) {

    sc.setAttribute(ServiceContext.GROUP, group);
  }

  public static void setVersion(ServiceContext sc, String version) {

    sc.setAttribute(ServiceContext.VERSION, version);
  }

  public static void setReferenceApp(ServiceContext sc, String referenceApp) {

    sc.setAttribute(ServiceContext.REFERENCE_APP, referenceApp);
  }

  public static void setReferenceGroup(ServiceContext sc, String referenceGroup) {

    sc.setAttribute(ServiceContext.REFERENCE_GROUP, referenceGroup);
  }

  public static void setReferenceVersion(ServiceContext sc, String referenceVersion) {

    sc.setAttribute(ServiceContext.REFERENCE_VERSION, referenceVersion);
  }

  public static void setReferenceConfig(ServiceContext sc, EndpointConfig config) {

    sc.setAttribute(ServiceContext.REFERENCE_CONFIG, config);
  }

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

  public static void setRequestParams(ServiceContext sc, Object[] params) {

    sc.setAttribute(ServiceContext.REQUEST_PARAMS, params);
  }

  public static void setRequestAttachments(ServiceContext sc, Attachments attachments) {

    sc.setAttribute(ServiceContext.REQUEST_ATTACHMENTS, attachments);
  }

  public static void setResponse(ServiceContext sc, Response response) {

    sc.setAttribute(ServiceContext.RESPONSE, response);
  }

  public static void setSerivePhase(ServiceContext sc, ProcessPhase phase) {
    sc.setAttribute(ServiceContext.SERVICE_PHASE, phase);
  }


}
