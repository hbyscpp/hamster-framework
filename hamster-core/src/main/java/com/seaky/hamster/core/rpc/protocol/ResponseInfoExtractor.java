package com.seaky.hamster.core.rpc.protocol;


public interface ResponseInfoExtractor<Rsp> {

	ResponseInfo extractor(Rsp rsp);
}
