package com.seaky.hamster.core.rpc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.service.ServiceContext;

@ServiceInterceptorAnnotation(name = "log", phases = {ProcessPhase.SERVER_CALL_SERVICE})
public class LogInterceptor extends DefaultServiceInterceptor {

  private static Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

  @Override
  public boolean preServerProcess(ServiceContext context) {

    long curTime = System.currentTimeMillis();
    context.setAtrr("_accessTime_", curTime);
    return true;
  }

  @Override
  protected void serverCompleteProcess(ServiceContext context, Throwable e) {

    long curTime = System.currentTimeMillis();
    long costTime = curTime - (long) context.getAttr("_accessTime_");
    if (e != null) {
      logger
          .info(
              "service:{} accept request from app:{},req version :{},address:{},port:{},cost time {} ms,error {}",
              context.getServiceName(), context.getReferApp(), context.getRequestInfo()
                  .getVersion(), context.getClientHost(), context.getClientPort(), costTime, e);
    } else {
      logger
          .info(
              "service:{} accept request from app:{},req version :{},address:{},port:{},cost time {} ms",
              context.getServiceName(), context.getReferApp(), context.getRequestInfo()
                  .getVersion(), context.getClientHost(), context.getClientPort(), costTime);
    }
  }

  @Override
  protected boolean preClientProcess(ServiceContext context) {
    long curTime = System.currentTimeMillis();
    context.setAtrr("_accessTime_", curTime);
    return true;
  }


  @Override
  protected void clientCompleteProcess(ServiceContext context, Throwable e) {

    long curTime = System.currentTimeMillis();
    long costTime = curTime - (long) context.getAttr("_accessTime_");
    if (e != null) {
      logger.info(
          "service:{}  from app:{},req version :{},address:{},port:{},cost time {} ms,error {}",
          context.getServiceName(), context.getReferApp(), context.getRequestInfo().getVersion(),
          context.getClientHost(), context.getClientPort(), costTime, e);
    } else {
      logger.info("service:{}  from app:{},req version :{},address:{},port:{},cost time {} ms",
          context.getServiceName(), context.getReferApp(), context.getRequestInfo().getVersion(),
          context.getClientHost(), context.getClientPort(), costTime);
    }
  }


}
