package com.seaky.hamster.core.rpc.protocol;



public interface RequestAttributeWriter<Req> {

	void write(Req req,RequestInfo info);

}
