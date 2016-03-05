package com.seaky.hamster.core.rpc.exception;

import java.net.InetSocketAddress;

import com.seaky.hamster.core.rpc.utils.Utils;

@SuppressWarnings("serial")
public class MismatchProtocolException extends RpcException {

	public MismatchProtocolException(Class<?> serverReqCls,
			Class<?> clientReqCls, InetSocketAddress serverAddr,
			InetSocketAddress clientAddr) {
		super(MISMATCH_PROTOCOL, "Server request type is "
				+ serverReqCls.getName() + " server addr is "
				+ Utils.socketAddrToString(serverAddr)
				+ ",client request type is " + clientReqCls.getName()
				+ " client addr is " + Utils.socketAddrToString(clientAddr));
	}

}
