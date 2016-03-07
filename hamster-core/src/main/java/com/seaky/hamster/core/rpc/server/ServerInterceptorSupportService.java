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
    boolean isSuccess = preProcess(context, interceptors);
    if (!isSuccess) {
      return;
    }
    try {
      Object result = service.process(ServiceContextUtils.getRequestParams(context));
      ServiceContextUtils.getResponse(context).setResult(result);
      postProcess(context, interceptors,null);
    } catch (Exception e) {
    	postProcess(context, interceptors, e);
      return;
    }
  }

}
