package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class NoServiceAvailable extends RpcException{

	public NoServiceAvailable(String msg) {
		super(NO_SERVICE_AVAILABLE,msg);
	}

}
