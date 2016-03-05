package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class UnknowException extends RpcException {

	public UnknowException(String msg) {
		super(UNKNOW_EXCEPTION, msg);
	}

}
