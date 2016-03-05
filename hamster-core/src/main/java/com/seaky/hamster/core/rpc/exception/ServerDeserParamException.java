package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServerDeserParamException extends RpcException{

	public ServerDeserParamException(String msg) {
		super(SERVER_DESER_EXCEPTION, msg);
		
	}

}
