package com.seaky.hamster.core.rpc.server;

import java.util.List;

import com.seaky.hamster.core.rpc.interceptor.InterceptorSupportService;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.service.JavaService;
import com.seaky.hamster.core.service.ServiceContext;

public class ServerInterceptorSupportService<Req, Rsp> extends InterceptorSupportService<Req, Rsp> {

  public ServerInterceptorSupportService(ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    super(protocolExtensionFactory);
  }

  public void process(ServiceContext context, JavaService service,
      List<ServiceInterceptor> interceptors) {
    boolean isSuccess = preProcess(context, interceptors);
    if (!isSuccess) {
      return;
    }
    try {
      Object result = service.process(context.getRequestInfo().getParams());
      context.getResponseInfo().setResult(result);
      postProcess(context, interceptors);
      triggerComplete(context, interceptors, null);
    } catch (Exception e) {
      triggerComplete(context, interceptors, e);
      return;
    }
  }

}
