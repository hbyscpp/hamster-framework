package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ErrorSendToRemoteServerException extends AccessRemoteServerException{

	public ErrorSendToRemoteServerException(String serviceName,
			String addr, Throwable e) {
		super(SEND_ERROR, serviceName, addr, e);
	}

}
