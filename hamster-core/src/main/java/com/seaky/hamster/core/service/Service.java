package com.seaky.hamster.core.service;


/**
 * 
 * 服务的抽象。所谓服务就是针对某个请求的响应。
 * 
 * @see JavaService
 *  
 * @author seaky 
 * @version @param <Req> 服务的请求
 * @version @param <Rsp> 服务的响应
 * @since 1.0.0
 */
public interface Service<Req, Rsp> {

	/**
	 * 处理请求
	 * @param request 请求
	 * @return 响应
	 */
	Rsp process(Req request) throws Exception;
	
}
