package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class NotSetResultException extends RpcException {

	public NotSetResultException() {
		super(NOT_SET_RESULT, "not set result");
	}

}
