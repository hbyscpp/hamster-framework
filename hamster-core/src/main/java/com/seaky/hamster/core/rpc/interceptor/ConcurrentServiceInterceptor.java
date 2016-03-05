package com.seaky.hamster.core.rpc.interceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.exception.ServiceReachMaxConcurrent;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.ServiceContext;

@ServiceInterceptorAnnotation(name = "current", phases = {
		ProcessPhase.SERVER_CALL_SERVICE,
		ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE })
public class ConcurrentServiceInterceptor extends DefaultServiceInterceptor {
	private static ConcurrentHashMap<String, AtomicInteger> serviceCurrentAccessNumer = new ConcurrentHashMap<String, AtomicInteger>();

	protected boolean preServerProcess(ServiceContext context) {

		int max = getConcurrentNum(context.getServiceConfig(),true);
		if (max <= 0)
			return true;
		boolean isOk = checkReachMaxConcurrent(
				Utils.generateKey("server", context.getServiceName(),
						context.getApp(), context.getServiceVersion()), max);
		if (isOk)
			return true;
		context.getResponseInfo().setResult(
				new ServiceReachMaxConcurrent(context.getApp(), context
						.getServiceName(), context.getServiceVersion(), true));

		return false;
	}

	protected boolean preClientProcess(ServiceContext context) {
		int max = getConcurrentNum(context.getReferConfig(),false);
		if (max <= 0)
			return true;
		boolean isOk = checkReachMaxConcurrent(
				Utils.generateKey("client", context.getServiceName(),
						context.getReferApp(), context.getReferVersion()), max);
		if (isOk)
			return true;
		context.getResponseInfo().setResult(
				new ServiceReachMaxConcurrent(context.getReferApp(), context
						.getServiceName(), context.getReferVersion(), false));

		return false;
	}

	protected void serverCompleteProcess(ServiceContext context, Throwable e) {

		decrCurConcurrentNum(Utils.generateKey("server",
				context.getServiceName(), context.getApp(),
				context.getServiceVersion()));
	}

	protected void clientCompleteProcess(ServiceContext context, Throwable e) {
		decrCurConcurrentNum(Utils.generateKey("client",
				context.getServiceName(), context.getReferApp(),
				context.getReferVersion()));
	}

	private boolean checkReachMaxConcurrent(String key, int maxConcurrent) {
		int curNum = addCurConcurrentNum(key);
		if (curNum > maxConcurrent) {
			decrCurConcurrentNum(key);
			return false;
		}
		return true;

	}

	public int addCurConcurrentNum(String key) {
		AtomicInteger currentNum = serviceCurrentAccessNumer.get(key);
		if (currentNum == null) {
			currentNum = new AtomicInteger();
			AtomicInteger oldNum = serviceCurrentAccessNumer.putIfAbsent(key,
					currentNum);
			if (oldNum != null) {
				currentNum = oldNum;
			}
		}
		return currentNum.incrementAndGet();
	}

	public void decrCurConcurrentNum(String key) {
		AtomicInteger currentNum = serviceCurrentAccessNumer.get(key);
		if (currentNum == null) {
			return;
		}
		currentNum.decrementAndGet();
	}

	private int getConcurrentNum(EndpointConfig serviceConfig,boolean isServer) {
		
		if(isServer)

		return serviceConfig.getValueAsInt(
				ConfigConstans.PROVIDER_MAX_CONCURRENT,
				ConfigConstans.PROVIDER_MAX_CONCURRENT_DEFAULT);
		else
			return serviceConfig.getValueAsInt(
					ConfigConstans.REFERENCE_MAX_CONCURRENT,
					ConfigConstans.REFERENCE_MAX_CONCURRENT_DEFAULT);
		}
}
