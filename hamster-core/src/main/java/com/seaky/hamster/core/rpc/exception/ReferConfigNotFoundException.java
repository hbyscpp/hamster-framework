package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ReferConfigNotFoundException extends RpcException {

	public ReferConfigNotFoundException(String app, String serviceName,
			String key) {
		super(REFER_CONFIG_NOT_FOUND, "referApp is " + app + " ,service name is "
				+ serviceName + ",key is " + key);
	}

}
