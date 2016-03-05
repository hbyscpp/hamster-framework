package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ErrorConnectRemoteServerException extends AccessRemoteServerException{

	public ErrorConnectRemoteServerException(String serviceName,
			String addr, Throwable e) {
		super(CONNECT_ERROR, serviceName, addr, e);
	}



}
