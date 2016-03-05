package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class LossReqParamException extends RpcException{

	public LossReqParamException(String paramName) {
		super(LOSS_REQ_PARAM, "no param " +paramName +" found in the request" );
	}

}
