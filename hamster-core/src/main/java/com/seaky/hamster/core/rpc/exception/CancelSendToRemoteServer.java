package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class CancelSendToRemoteServer extends AccessRemoteServerException{

	public CancelSendToRemoteServer(String serviceName, String addr,
			Throwable e) {
		super(SEND_CANCEL, serviceName, addr, e);
	}


}
