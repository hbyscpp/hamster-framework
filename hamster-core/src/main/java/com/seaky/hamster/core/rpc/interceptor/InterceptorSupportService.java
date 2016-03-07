package com.seaky.hamster.core.rpc.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.Response;

// 支持拦截器的service
public abstract class InterceptorSupportService<Req, Rsp> {

	protected ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

	private static Logger logger = LoggerFactory
			.getLogger(InterceptorSupportService.class);

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
					boolean isNotContine = ServiceContextUtils.getResponse(
							context).isDone();
					if (isNotContine) {
						postProcess(context, interceptors, i, null);
						return false;
					}
				} catch (Exception e) {
					postProcess(context, interceptors, i, e);
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
					Response rsp = ServiceContextUtils.getResponse(context);
					rsp.setResult(e1);
				}
			}
		}
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
