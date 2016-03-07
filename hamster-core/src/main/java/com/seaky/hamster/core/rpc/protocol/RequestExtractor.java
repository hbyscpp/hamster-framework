package com.seaky.hamster.core.rpc.protocol;

import com.seaky.hamster.core.rpc.common.ServiceContext;



public interface RequestExtractor<Req> {

  // 抽取 app，refer app,service name,group,refer group,version,refer version,attachment,请求参数信息
  // 如果抽取错误，抛出异常或者在response中设置异常
  void extractTo(Req req, ServiceContext context);

  // 从context抽取 app，refer app,service name,group,refer group,version,refer version,attachment,请求参数信息
  Req extractFrom(ServiceContext context);
}
