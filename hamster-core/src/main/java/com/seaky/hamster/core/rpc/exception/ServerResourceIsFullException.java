package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServerResourceIsFullException extends RpcException {

	public ServerResourceIsFullException(String resource) {
		super(SERVER_RES_ISFULL, resource +" is exhausting");
	}


}
