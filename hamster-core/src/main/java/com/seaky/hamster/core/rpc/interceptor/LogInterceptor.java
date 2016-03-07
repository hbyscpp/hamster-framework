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
  public boolean preServerProcess(ServiceContext context) {

    long curTime = System.currentTimeMillis();
    context.setAttribute("_accessTime_", curTime);
    return true;
  }

  @Override
  protected void serverCompleteProcess(ServiceContext context, Throwable e) {

    long curTime = System.currentTimeMillis();
    long costTime = curTime - (long) context.getAttribute("_accessTime_");
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
  protected boolean preClientProcess(ServiceContext context) {
    long curTime = System.currentTimeMillis();
    context.setAttribute("_accessTime_", curTime);
    return true;
  }


  @Override
  protected void clientCompleteProcess(ServiceContext context, Throwable e) {

    long curTime = System.currentTimeMillis();
    long costTime = curTime - (long) context.getAttribute("_accessTime_");
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
