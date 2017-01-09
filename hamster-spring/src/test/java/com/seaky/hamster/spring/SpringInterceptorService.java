package com.seaky.hamster.spring;

import org.springframework.stereotype.Service;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;

@ServiceInterceptorAnnotation(name = "testInput", creator = "spring",
    phases = {ProcessPhase.SERVER_CALL_SERVICE})
@Service
public class SpringInterceptorService implements ServiceInterceptor {

  @Override
  public void preProcess(ServiceContext context) throws Exception {

    System.out
        .println("SpringInterceptorService test " + ServiceContextUtils.getClientHost(context));
  }

  @Override
  public void postProcess(ServiceContext context) {

  }

}
