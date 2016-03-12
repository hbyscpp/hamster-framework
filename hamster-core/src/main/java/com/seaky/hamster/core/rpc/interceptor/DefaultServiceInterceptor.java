package com.seaky.hamster.core.rpc.interceptor;

import com.seaky.hamster.core.rpc.common.ServiceContext;


public abstract class DefaultServiceInterceptor implements ServiceInterceptor {

  @Override
  public void preProcess(ServiceContext context) {
    if (context.processPhase() == ProcessPhase.SERVER_CALL_SERVICE)
      preServerProcess(context);
    else if (context.processPhase() == ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE)
      preClientProcess(context);
    else if (context.processPhase() == ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE) {
      preClientClusterProcess(context);
    }
  }

  protected void preServerProcess(ServiceContext context) {}

  protected void preClientProcess(ServiceContext context) {}

  protected void preClientClusterProcess(ServiceContext context) {}


  @Override
  public void postProcess(ServiceContext context) {
    if (context.processPhase() == ProcessPhase.SERVER_CALL_SERVICE)
      postServerProcess(context);
    else if (context.processPhase() == ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE)
      postClientProcess(context);
    else if (context.processPhase() == ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE) {
      postClientClusterProcess(context);
    }
  }

  protected void postServerProcess(ServiceContext context) {

  }

  protected void postClientProcess(ServiceContext context) {

  }

  protected void postClientClusterProcess(ServiceContext context) {}

}
