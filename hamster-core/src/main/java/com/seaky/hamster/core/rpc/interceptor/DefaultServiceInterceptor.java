package com.seaky.hamster.core.rpc.interceptor;

import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;


public abstract class DefaultServiceInterceptor implements ServiceInterceptor {

  @Override
  public void preProcess(ServiceContext context) {
    if (ServiceContextUtils.getSerivePhase(context) == ProcessPhase.SERVER_CALL_SERVICE)
       preServerProcess(context);
    else
       preClientProcess(context);
  }

  protected void preServerProcess(ServiceContext context) {
  }

  protected void preClientProcess(ServiceContext context) {
  }



  @Override
  public void postProcess(ServiceContext context) {
    if (ServiceContextUtils.getSerivePhase(context) == ProcessPhase.SERVER_CALL_SERVICE)
      postServerProcess(context);
    else
      postClientProcess(context);
  }

  protected void postServerProcess(ServiceContext context) {

  }

  protected void postClientProcess(ServiceContext context) {

  }

}
