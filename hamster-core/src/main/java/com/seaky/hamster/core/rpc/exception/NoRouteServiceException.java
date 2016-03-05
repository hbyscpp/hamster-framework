package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class NoRouteServiceException extends RpcException {

	public NoRouteServiceException(String msg) {
		super(NO_ROUTE_SERVICE, msg);
	}

}
