package com.seaky.hamster.core.rpc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;

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
    boolean isException = ServiceContextUtils.getResponseHeader(context).isException();
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
    if (isException) {
      serverlogger.info("{}:{}:{}:{} access from {}:{}:{}:{}:{},seq num {},cost {} ms\n",
          header.getServiceName(), header.getApp(), header.getVersion(), header.getGroup(),
          ServiceContextUtils.getClientHost(context), ServiceContextUtils.getClientPort(context),
          header.getReferenceApp(), header.getReferenceVersion(), header.getReferenceGroup(),
          header.getAttachments().getAsLong(Constants.SEQ_NUM_KEY), costTime,
          ServiceContextUtils.getResponseBody(context).getResult());
    } else {
      serverlogger.info("{}:{}:{}:{} access from {}:{}:{}:{}:{},seq num {},cost {} ms",
          header.getServiceName(), header.getApp(), header.getVersion(), header.getGroup(),
          ServiceContextUtils.getClientHost(context), ServiceContextUtils.getClientPort(context),
          header.getReferenceApp(), header.getReferenceVersion(), header.getReferenceGroup(),
          header.getAttachments().getAsLong(Constants.SEQ_NUM_KEY), costTime);
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
    boolean isException = ServiceContextUtils.getResponseHeader(context).isException();
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);

    if (isException) {
      clientlogger.info("{}:{}:{}:{} request to {}:{}:{}:{}:{},traceId {},seqNum {},cost {} ms\n",
          header.getServiceName(), header.getApp(), header.getVersion(), header.getGroup(),
          ServiceContextUtils.getClientHost(context), ServiceContextUtils.getClientPort(context),
          header.getReferenceApp(), header.getReferenceVersion(), header.getReferenceGroup(),
          header.getAttachments().getAsLong(Constants.SEQ_NUM_KEY),
          ServiceContextUtils.getInterceptorExceptionTrace(context).getTraceId(), costTime,
          ServiceContextUtils.getResponseBody(context).getResult());
    } else {
      clientlogger.info("{}:{}:{}:{} request to {}:{}:{}:{}:{},traceId {},seqNum {},cost {} ms",
          header.getServiceName(), header.getApp(), header.getVersion(), header.getGroup(),
          ServiceContextUtils.getClientHost(context), ServiceContextUtils.getClientPort(context),
          header.getReferenceApp(), header.getReferenceVersion(), header.getReferenceGroup(),
          header.getAttachments().getAsLong(Constants.SEQ_NUM_KEY),
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
    boolean isException = ServiceContextUtils.getResponseHeader(context).isException();
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
    if (isException) {
      clientlogger.info("{}:{}:{}:{} call traceid {},cost {} ms\n", header.getServiceName(),
          header.getReferenceApp(), header.getReferenceVersion(), header.getReferenceGroup(),
          ServiceContextUtils.getInterceptorExceptionTrace(context).getTraceId(), costTime,
          ServiceContextUtils.getResponseBody(context).getResult());
    } else {
      clientlogger.info("{}:{}:{}:{} call traceid {},cost {} ms", header.getServiceName(),
          header.getReferenceApp(), header.getReferenceVersion(), header.getReferenceGroup(),
          ServiceContextUtils.getInterceptorExceptionTrace(context).getTraceId(), costTime);
    }
  }

}
