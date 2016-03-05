package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServiceSigMismatchException extends RpcException{

	public ServiceSigMismatchException(String msg) {
		super(SIG_MISMATCH_EXCEPTION, msg);
	}

}
