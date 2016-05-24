package com.seaky.hamster.core.rpc.server;

import java.util.List;

import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.exception.BusinessException;
import com.seaky.hamster.core.rpc.interceptor.InterceptorSupportService;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaService;

import rx.Observable;

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
      Observable<Object> result = service.process(ServiceContextUtils.getRequestParams(context));
      ServiceContextUtils.getResponse(context).setResult(result.toBlocking().first());
    } catch (Exception e) {
      BusinessException be = new BusinessException(Utils.getActualException(e));
      ServiceContextUtils.getResponse(context).setResult(be);
    } finally {
      postProcess(context, interceptors);
    }
  }

}
