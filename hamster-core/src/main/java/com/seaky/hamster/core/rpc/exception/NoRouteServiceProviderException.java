package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class NoRouteServiceProviderException extends RpcException {

	public NoRouteServiceProviderException(String msg) {
		super(NO_ROUTE_SERVICE_PROVIDER, msg);
	}

}
