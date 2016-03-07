package com.seaky.hamster.core.rpc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;

@ServiceInterceptorAnnotation(name = "log", phases = {ProcessPhase.SERVER_CALL_SERVICE})
public class LogInterceptor extends DefaultServiceInterceptor {

  private static Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

  @Override
  public void preServerProcess(ServiceContext context) {

    long curTime = System.currentTimeMillis();
    context.setAttribute("_accessTime_", curTime);
  }

  @Override
  protected void postServerProcess(ServiceContext context) {

    long curTime = System.currentTimeMillis();
    long costTime = curTime - (long) context.getAttribute("_accessTime_");
    Throwable e=ServiceContextUtils.getResponse(context).getException();
    if (e != null) {
      logger.info(
          "service:{} accept request from app:{},req version :{},address:{},port:{},cost time {} ms,error {}",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getVersion(context), ServiceContextUtils.getClientHost(context),
          ServiceContextUtils.getClientPort(context), costTime, e);
    } else {
      logger.info(
          "service:{} accept request from app:{},req version :{},address:{},port:{},cost time {} ms",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getVersion(context), ServiceContextUtils.getClientHost(context),
          ServiceContextUtils.getClientPort(context), costTime);
    }
  }

  @Override
  protected void preClientProcess(ServiceContext context) {
    long curTime = System.currentTimeMillis();
    context.setAttribute("_accessTime_", curTime);
  }


  @Override
  protected void postClientProcess(ServiceContext context) {

    long curTime = System.currentTimeMillis();
    long costTime = curTime - (long) context.getAttribute("_accessTime_");
    Throwable e=ServiceContextUtils.getResponse(context).getException();
    if (e != null) {
      logger.info(
          "service:{} accept request from app:{},req version :{},address:{},port:{},cost time {} ms,error {}",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getVersion(context), ServiceContextUtils.getClientHost(context),
          ServiceContextUtils.getClientPort(context), costTime, e);
    } else {
      logger.info(
          "service:{} accept request from app:{},req version :{},address:{},port:{},cost time {} ms",
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getVersion(context), ServiceContextUtils.getClientHost(context),
          ServiceContextUtils.getClientPort(context), costTime);
    }
  }


}
