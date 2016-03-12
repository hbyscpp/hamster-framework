package com.seaky.hamster.core.rpc.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.trace.InterceptorChainExceptionTrace;

// 支持拦截器的service
public abstract class InterceptorSupportService<Req, Rsp> {

	protected ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

	public InterceptorSupportService(
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
		this.protocolExtensionFactory = protocolExtensionFactory;
	}

	public boolean preProcess(ServiceContext context,
			List<ServiceInterceptor> interceptors) {
		if (interceptors != null && interceptors.size() > 0) {
			int size = interceptors.size();
			for (int i = 0; i < size; ++i) {
				ServiceInterceptor interceptor = interceptors.get(i);
				try {
					interceptor.preProcess(context);
					boolean isDone = ServiceContextUtils.getResponse(context)
							.isDone();
					if (isDone) {
						postProcess(context, interceptors, i + 1, null);
						return false;
					}
				} catch (Exception e) {
					addTrace(context, interceptor, true, e);
					postProcess(context, interceptors, i + 1, e);
					return false;
				}
			}
		}
		return true;

	}

	public void postProcess(ServiceContext context,
			List<ServiceInterceptor> interceptors, Throwable e) {

		postProcess(context, interceptors, interceptors == null ? 0
				: interceptors.size(), e);
	}

	public void postProcess(ServiceContext context,
			List<ServiceInterceptor> interceptors, int index, Throwable e) {

		if (e != null) {
			ServiceContextUtils.getResponse(context).setResult(e);
		}
		if (interceptors != null && interceptors.size() > 0) {
			for (int i = 0; i < index; ++i) {
				ServiceInterceptor interceptor = interceptors
						.get(index - i - 1);
				try {
					interceptor.postProcess(context);
				} catch (Exception e1) {
					addTrace(context, interceptor, false, e);
					Response rsp = ServiceContextUtils.getResponse(context);
					rsp.setResult(e1);
				}
			}
		}
	}

	private void addTrace(ServiceContext context, ServiceInterceptor obj,
			boolean isPre, Throwable e) {
		InterceptorChainExceptionTrace exceptionTrace = ServiceContextUtils
				.getInterceptorExceptionTrace(context);
		if (exceptionTrace == null) {
			exceptionTrace = new InterceptorChainExceptionTrace();
			ServiceContextUtils.setInterceptorExceptionTrace(context,
					exceptionTrace);
		}
		exceptionTrace.addException(obj, false, e);
	}

	public void setException(Throwable e, Response info) {
		if (e instanceof InvocationTargetException) {
			info.setResult(((InvocationTargetException) e).getTargetException());
		} else {
			info.setResult(e);
		}
	}

	// 优先设置异常信息
	public void setFuture(ServiceContext sc, SettableFuture<Object> result) {
		Response responseInfo = ServiceContextUtils.getResponse(sc);
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
