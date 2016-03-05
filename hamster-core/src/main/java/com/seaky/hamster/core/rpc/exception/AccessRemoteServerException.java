package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public abstract class AccessRemoteServerException extends RpcException{

	public AccessRemoteServerException(int code, String serviceName,String addr,Throwable e) {
		super(code, serviceName+addr, e);
	}

}
