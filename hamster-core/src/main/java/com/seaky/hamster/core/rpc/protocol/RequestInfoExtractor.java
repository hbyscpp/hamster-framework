package com.seaky.hamster.core.rpc.protocol;

import com.seaky.hamster.core.service.ServiceContext;



public interface RequestInfoExtractor<Req> {

	void extract(Req req,ServiceContext context);
}
