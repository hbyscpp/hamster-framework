package com.seaky.hamster.core.rpc.interceptor;

import java.util.List;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseBody;
import com.seaky.hamster.core.rpc.trace.ClientCallExceptionTrace;

// 支持拦截器的service
public abstract class InterceptorSupportService<Req, Rsp> {

  protected ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  public InterceptorSupportService(ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    this.protocolExtensionFactory = protocolExtensionFactory;
  }

  public boolean preProcess(ServiceContext context, List<ServiceInterceptor> interceptors) {
    if (interceptors != null && interceptors.size() > 0) {
      int size = interceptors.size();
      for (int i = 0; i < size; ++i) {
        ServiceInterceptor interceptor = interceptors.get(i);
        try {
          interceptor.preProcess(context);
          boolean isDone = ServiceContextUtils.getResponseBody(context).isDone();
          if (isDone) {
            postProcess(context, interceptors, i + 1);
            return true;
          }
        } catch (Exception e) {
          ServiceContextUtils.getResponseBody(context).setResult(e);
          ServiceContextUtils.getResponseHeader(context).setException(true);
          addTrace(context, interceptor, true, e);
          postProcess(context, interceptors, i + 1);
          return true;
        }
      }
    }
    return false;

  }

  public void postProcess(ServiceContext context, List<ServiceInterceptor> interceptors) {

    postProcess(context, interceptors, interceptors == null ? 0 : interceptors.size());
  }

  public void postProcess(ServiceContext context, List<ServiceInterceptor> interceptors,
      int index) {
    if (interceptors != null && interceptors.size() > 0) {
      for (int i = 0; i < index; ++i) {
        ServiceInterceptor interceptor = interceptors.get(index - i - 1);
        try {
          interceptor.postProcess(context);
        } catch (Exception e1) {
          addTrace(context, interceptor, false, e1);
          ProtocolResponseBody rsp = ServiceContextUtils.getResponseBody(context);
          rsp.setResult(e1);
          ServiceContextUtils.getResponseHeader(context).setException(true);

        }
      }
    }
  }

  private void addTrace(ServiceContext context, ServiceInterceptor obj, boolean isPre,
      Throwable e) {

    if (context.processPhase() == ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE) {
      if (isPre) {
        addException(context, ClientCallExceptionTrace.CALL_CLUSTER_SERVICE_PREPROCESS,
            constructExceptionMsg(context, obj), e);
      } else {
        addException(context, ClientCallExceptionTrace.CALL_CLUSTER_SERVICE_POSTPROCESS,
            constructExceptionMsg(context, obj), e);
      }
    } else if (context.processPhase() == ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE) {
      if (isPre) {
        addException(context, ClientCallExceptionTrace.CALL_SERVICE_INSTANCE_PREPROCESS,
            constructExceptionMsg(context, obj), e);
      } else {
        addException(context, ClientCallExceptionTrace.CALL_SERVICE_INSTANCE_POSTPROCESS,
            constructExceptionMsg(context, obj), e);
      }
    }
  }

  public void addException(ServiceContext context, String phase, String msg, Throwable e) {
    ClientCallExceptionTrace exceptionTrace =
        ServiceContextUtils.getInterceptorExceptionTrace(context);
    exceptionTrace.addException(phase, msg, e);
  }

  private String constructExceptionMsg(ServiceContext sc, ServiceInterceptor obj) {
    StringBuilder sb = new StringBuilder();
    sb.append("service:").append(ServiceContextUtils.getRequestHeader(sc).getServiceName());
    sb.append(",interceptor:").append(obj.getClass().getName());
    return sb.toString();
  }

  // 优先设置异常信息
  public void setFuture(ServiceContext sc, SettableFuture<Object> result) {
    ProtocolResponseBody responseInfo = ServiceContextUtils.getResponseBody(sc);
    if (responseInfo.isDone()) {

      if (ServiceContextUtils.getResponseHeader(sc).isException()) {
        result.setException((Throwable) responseInfo.getResult());
      } else {
        result.set(responseInfo.getResult());
      }
    } else {
      result.set(new NotSetResultException());
    }
  }
}
