package com.seaky.hamster.core.rpc.protocol;



public interface RequestConvertor<Req> {

	Req convert(RequestInfo info);

}
