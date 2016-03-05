package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServiceNotFoundException extends RpcException {

	public ServiceNotFoundException(String msg) {
		super(SC_NOT_FOUNT, msg);
	}

	

}
