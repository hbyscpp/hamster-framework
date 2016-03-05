package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ForbiddenException extends RpcException {

	public ForbiddenException(String msg) {
		super(FORBBDEN, "forbidden because " + msg);
	}

}
