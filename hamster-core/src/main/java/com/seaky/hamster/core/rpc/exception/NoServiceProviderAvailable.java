package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class NoServiceProviderAvailable extends RpcException{

	public NoServiceProviderAvailable(String msg) {
		super(NO_SERVICE_PROVIDER_AVAILABLE,msg);
	}

}
