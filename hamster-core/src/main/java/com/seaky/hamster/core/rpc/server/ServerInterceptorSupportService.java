package com.seaky.hamster.core.rpc.server;

import java.util.List;

import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.interceptor.InterceptorSupportService;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.service.JavaService;

public class ServerInterceptorSupportService<Req, Rsp> extends InterceptorSupportService<Req, Rsp> {

  public ServerInterceptorSupportService(
      ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    super(protocolExtensionFactory);
  }

  public void process(ServiceContext context, JavaService service,
      List<ServiceInterceptor> interceptors) {
    boolean isDone = preProcess(context, interceptors);
    if (isDone) {
      return;
    }
    try {
      Object result = service.process(ServiceContextUtils.getRequestParams(context));
      ServiceContextUtils.getResponse(context).setResult(result);
    } catch (Exception e) {
      ServiceContextUtils.getResponse(context).setResult(e);;
    } finally {
      postProcess(context, interceptors);
    }
  }

}
