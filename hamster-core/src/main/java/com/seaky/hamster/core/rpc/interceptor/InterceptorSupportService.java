package com.seaky.hamster.core.rpc.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.ResponseInfo;
import com.seaky.hamster.core.service.ServiceContext;

// 支持拦截器的service
public abstract class InterceptorSupportService<Req, Rsp> {

  protected ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private static Logger logger = LoggerFactory.getLogger(InterceptorSupportService.class);

  public InterceptorSupportService(ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    this.protocolExtensionFactory = protocolExtensionFactory;
  }

  public  boolean preProcess(Object context, List<ServiceInterceptor> interceptors) {
    if (interceptors != null && interceptors.size() > 0) {
      int size = interceptors.size();
      for (int i = 0; i < size; ++i) {
        ServiceInterceptor interceptor = interceptors.get(i);
        try {
          boolean isContinue = interceptor.preProcess(context);
          if (!isContinue) {
            triggerComplete(context, interceptors, i, null);
            return false;
          }
        } catch (Exception e) {
          triggerComplete(context, interceptors, i, e);
          return false;
        }
      }
    }
    return true;

  }

  public void postProcess(ServiceContext context, List<ServiceInterceptor> interceptors)
      throws Exception {
    if (interceptors != null) {
      int size = interceptors.size();
      for (int i = 0; i < size; ++i) {
        ServiceInterceptor interceptor = interceptors.get(size - i - 1);
        interceptor.postProcess(context);
      }
    }
  }

  protected void completeProcess(ServiceContext context, List<ServiceInterceptor> interceptors,
      int index, Throwable e) {
    if (interceptors != null && interceptors.size() > 0) {
      for (int i = 0; i < index; ++i) {
        ServiceInterceptor interceptor = interceptors.get(index - i - 1);
        try {
          interceptor.completeProcess(context, e);
        } catch (Exception e1) {
          logger.error("Service {} version {},interceptor {} completeProcess error.",
              context.getServiceName(), context.getServiceVersion(), interceptor.getClass(), e1);
        }
      }
    }
  }

 

  protected void triggerComplete(ServiceContext sc, List<ServiceInterceptor> interceptors,
      Throwable proEx) {
    triggerComplete(sc, interceptors, interceptors == null ? 0 : interceptors.size(), proEx);
  }

  public void triggerComplete(ServiceContext sc, List<ServiceInterceptor> interceptors, int index,
      Throwable proEx) {
    completeProcess(sc, interceptors, index, proEx);
    ResponseInfo info = sc.getResponseInfo();
    // 若未处理异常,则抛出底层未处理异常
    if (!info.isDone() && proEx != null) {
      setException(proEx, info);
    }
  }

  public void setException(Throwable e, ResponseInfo info) {
    if (e instanceof InvocationTargetException) {
      info.setResult(((InvocationTargetException) e).getTargetException());
    } else {
      info.setResult(e);
    }
  }

  // 优先设置异常信息
  public void setFuture(ServiceContext sc, SettableFuture<Object> result) {
    ResponseInfo responseInfo = sc.getResponseInfo();
    if (responseInfo.isDone()) {

      if (responseInfo.isException()) {
        result.setException(responseInfo.getException());
      } else {
        result.set(responseInfo.getResult());
      }
    } else {
      result.set(new NotSetResultException());
    }
  }
}
