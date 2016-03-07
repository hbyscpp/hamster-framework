package com.seaky.hamster.core.rpc.interceptor;

import com.seaky.hamster.core.rpc.common.ServiceContext;

/**
 * 
 * 服务调用的拦截器 拦截器的实现要保证线程安全 拦截器
 * 
 * @author seaky
 * @since 1.0.0
 */
public interface ServiceInterceptor {

	// 对于客户端，在发送请求之前执行
	// 对于服务端，在调用服务实现之前执行
	// 如果返回false或者抛出异常则不执行后面的interceptor,也不会执行postProcess，但是会执行completeProcess
	void preProcess(ServiceContext context) throws Exception;

	// 用于调用完成之后的处理
	void postProcess(ServiceContext context);

}
