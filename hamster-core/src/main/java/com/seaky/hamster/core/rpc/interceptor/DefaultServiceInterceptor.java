package com.seaky.hamster.core.rpc.interceptor;

import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;


public abstract class DefaultServiceInterceptor implements ServiceInterceptor {

  @Override
  public boolean preProcess(ServiceContext context) {
    if (ServiceContextUtils.getSerivePhase(context) == ProcessPhase.SERVER_CALL_SERVICE)
      return preServerProcess(context);
    else
      return preClientProcess(context);
  }

  protected boolean preServerProcess(ServiceContext context) {
    return true;
  }

  protected boolean preClientProcess(ServiceContext context) {
    return true;
  }

  @Override
  public void completeProcess(ServiceContext context, Throwable e) throws Exception {
    if (ServiceContextUtils.getSerivePhase(context) == ProcessPhase.SERVER_CALL_SERVICE)
      serverCompleteProcess(context, e);
    else
      clientCompleteProcess(context, e);
  };

  protected void serverCompleteProcess(ServiceContext context, Throwable e) {

  }

  protected void clientCompleteProcess(ServiceContext context, Throwable e) {

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
