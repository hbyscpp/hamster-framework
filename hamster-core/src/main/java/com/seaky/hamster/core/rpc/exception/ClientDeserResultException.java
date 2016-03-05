package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ClientDeserResultException extends RpcException{
	public ClientDeserResultException(Throwable e) {
		super(CLIENT_DESER_EXCEPTION,e);
		
	}
}
