package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class NoServiceMatchException extends RpcException {

	public NoServiceMatchException(String msg) {
		super(NO_SERVICE_MATCH, msg);
	}

	

}
