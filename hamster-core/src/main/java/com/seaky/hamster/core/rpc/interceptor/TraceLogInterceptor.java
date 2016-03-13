package com.seaky.hamster.core.rpc.interceptor;


import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.trace.ClientCallExceptionTrace;

@ServiceInterceptorAnnotation(name = "tracelog", phases = {
    ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE})
public class TraceLogInterceptor extends DefaultServiceInterceptor {



  @Override
  protected void postServerProcess(ServiceContext context) {
	 ClientCallExceptionTrace trace= ServiceContextUtils.getInterceptorExceptionTrace(context);
	 trace.show();
  }


 



  @Override
  protected void postClientClusterProcess(ServiceContext context) {}

}
