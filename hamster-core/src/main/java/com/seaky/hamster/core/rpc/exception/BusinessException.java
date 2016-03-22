package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class BusinessException extends RpcException {
	public BusinessException(Throwable e) {
		super(BUSINESS_EXCEPTION, e);
	}

}
