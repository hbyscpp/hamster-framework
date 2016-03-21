package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class NoServiceProviderMatchException extends RpcException {

	public NoServiceProviderMatchException(String msg) {
		super(NO_SERVICE_PROVIDER_MATCH, msg);
	}

	

}
