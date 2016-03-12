package com.seaky.hamster.core.rpc.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;

/**
 * 记录ServiceInterceptor处理过程之中抛出的异常
 * @author seaky
 * @since 1.0.0
 * @Date Mar 12, 2016 
 */
public class InterceptorChainExceptionTrace {

	private List<ExceptionTrace> traces=new ArrayList<ExceptionTrace>();
	
	public void addException(ServiceInterceptor obj,boolean isPre,Throwable e)
	{
		ExceptionTrace t=new ExceptionTrace();
		t.e=e;
		t.isPre=isPre;
		t.obj=obj;
		traces.add(t);
	}
	private static class ExceptionTrace
	{
		public ServiceInterceptor obj;
		
		public boolean isPre;
		
		public Throwable e;
	}
}
