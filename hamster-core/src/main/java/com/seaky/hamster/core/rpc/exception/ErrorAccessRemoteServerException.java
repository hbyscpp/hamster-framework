package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ErrorAccessRemoteServerException extends AccessRemoteServerException{
	public ErrorAccessRemoteServerException(String serviceName,
			String addr, Throwable e) {
		super(ACCESS_ERROR, serviceName, addr, e);
	}

}
