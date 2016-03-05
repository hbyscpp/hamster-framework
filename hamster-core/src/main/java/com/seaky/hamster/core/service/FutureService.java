package com.seaky.hamster.core.service;

import java.util.concurrent.Future;
/**
 * 
 * 异步请求响应服务
 *  
 * @author seaky 
 * @version @param <Req> 请求类型
 * @version @param <Rsp> 响应类型
 * @since 1.0.0
 */
public interface FutureService<Req, Rsp> {
	
	Future<Rsp> processAsyn(Req request);
}
