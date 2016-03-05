package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class CancelConnectToRemoteServerException extends AccessRemoteServerException{

	public CancelConnectToRemoteServerException(String serviceName,
			String addr, Throwable e) {
		super(CONNECT_CANCEL, serviceName, addr, e);
	}


}
