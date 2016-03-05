package com.seaky.hamster.core.service;

import rx.Observable;

/**
 * 
 * 反应式的服务
 * 
 * @author seaky
 * @version @param <Req> 请求类型
 * @version @param <Rsp> 响应类型
 * @since 1.0.0
 */
public interface ReactiveService<Req, Rsp> {

  Observable<Rsp> processReactive(Req request);
}
