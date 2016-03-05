package com.seaky.hamster.core.rpc.protocol;


//框架内部使用
//向结果写入附加属性
public interface ResponseAttributeWriter<Req,Rsp> {
	
	
	void write(Req req,Rsp rsp,ResponseInfo info);
	
}
