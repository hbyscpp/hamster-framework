package com.seaky.hamster.core.rpc.protocol;



public interface RequestInfoExtractor<Req> {

	RequestInfo extract(Req req);
}
