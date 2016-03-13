package com.seaky.hamster.core.rpc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;

@ServiceInterceptorAnnotation(name = "accesslog", phases = {ProcessPhase.SERVER_CALL_SERVICE,
    ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE, ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE})
public class AccessLogInterceptor extends DefaultServiceInterceptor {

  private static Logger serverlogger = LoggerFactory.getLogger("hamster_server_accesslog");

  private static Logger clientlogger = LoggerFactory.getLogger("hamster_client_accesslog");


  @Override
  public void preServerProcess(ServiceContext context) {

    setStartTime(context);
  }

  @Override
  protected void postServerProcess(ServiceContext context) {

    long costTime = getCostTime(context);
    Throwable e = ServiceContextUtils.getResponse(context).getException();
    if (e != null) {
      serverlogger.info("{}:{}:{}:{} access from {}:{}:{}:{}:{},seq num {},cost {} ms,error {}",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getApp(context),
          ServiceContextUtils.getVersion(context), ServiceContextUtils.getGroup(context),
          ServiceContextUtils.getClientHost(context), ServiceContextUtils.getClientPort(context),
          ServiceContextUtils.getReferenceApp(context), ServiceContextUtils.getVersion(context),
          ServiceContextUtils.getReferenceGroup(context),
          ServiceContextUtils.getRequestAttachments(context).getAttachment(Constants.SEQ_NUM_KEY),
          costTime, e);
    } else {
      serverlogger.info("{}:{}:{}:{} access from {}:{}:{}:{}:{},seq num {},cost {} ms",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getApp(context),
          ServiceContextUtils.getVersion(context), ServiceContextUtils.getGroup(context),
          ServiceContextUtils.getClientHost(context), ServiceContextUtils.getClientPort(context),
          ServiceContextUtils.getReferenceApp(context), ServiceContextUtils.getVersion(context),
          ServiceContextUtils.getReferenceGroup(context),
          ServiceContextUtils.getRequestAttachments(context).getAttachment(Constants.SEQ_NUM_KEY),
          costTime);
    }
  }

  @Override
  protected void preClientProcess(ServiceContext context) {
    setStartTime(context);
  }

  private void setStartTime(ServiceContext context) {
    long curTime = System.currentTimeMillis();
    context.setAttribute("_accessTime_", curTime);
  }

  private long getCostTime(ServiceContext context) {
    long curTime = System.currentTimeMillis();
    long costTime = curTime - (long) context.getAttribute("_accessTime_");
    return costTime;
  }


  @Override
  protected void postClientProcess(ServiceContext context) {

    long costTime = getCostTime(context);
    Throwable e = ServiceContextUtils.getResponse(context).getException();
    if (e != null) {
      clientlogger.info(
          "{}:{}:{}:{} request to {}:{}:{}:{}:{},traceId {},seqNum {},cost {} ms,error {}",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getReferenceVersion(context),
          ServiceContextUtils.getReferenceGroup(context),
          ServiceContextUtils.getServerHost(context), ServiceContextUtils.getServerPort(context),
          ServiceContextUtils.getApp(context), ServiceContextUtils.getVersion(context),
          ServiceContextUtils.getGroup(context),
          ServiceContextUtils.getRequestAttachments(context).getAttachment(Constants.SEQ_NUM_KEY),
          ServiceContextUtils.getInterceptorExceptionTrace(context).getTraceId(), costTime, e);
    } else {
      clientlogger.info("{}:{}:{}:{} request to {}:{}:{}:{}:{},traceId {},seqNum {},cost {} ms",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getReferenceVersion(context),
          ServiceContextUtils.getReferenceGroup(context),
          ServiceContextUtils.getServerHost(context), ServiceContextUtils.getServerPort(context),
          ServiceContextUtils.getApp(context), ServiceContextUtils.getVersion(context),
          ServiceContextUtils.getGroup(context),
          ServiceContextUtils.getRequestAttachments(context).getAttachment(Constants.SEQ_NUM_KEY),
          ServiceContextUtils.getInterceptorExceptionTrace(context).getTraceId(), costTime);
    }
  }

  @Override
  protected void preClientClusterProcess(ServiceContext context) {
    setStartTime(context);
  }

  @Override
  protected void postClientClusterProcess(ServiceContext context) {
    long costTime = getCostTime(context);
    Throwable e = ServiceContextUtils.getResponse(context).getException();
    if (e != null) {
      clientlogger.info("{}:{}:{}:{} call traceid {},cost {} ms,error {}",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getReferenceVersion(context),
          ServiceContextUtils.getReferenceGroup(context),
          ServiceContextUtils.getInterceptorExceptionTrace(context).getTraceId(), costTime, e);
    } else {
      clientlogger.info("{}:{}:{}:{} call traceid {},cost {} ms",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getReferenceVersion(context),
          ServiceContextUtils.getReferenceGroup(context),
          ServiceContextUtils.getInterceptorExceptionTrace(context).getTraceId(), costTime);
    }
  }

}
