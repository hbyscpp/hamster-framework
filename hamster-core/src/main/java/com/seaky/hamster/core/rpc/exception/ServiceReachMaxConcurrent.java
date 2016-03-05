package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServiceReachMaxConcurrent extends RpcException {

	public ServiceReachMaxConcurrent(String app, String serviceName,
			String key, boolean isServer) {
		super(SERVICE_REACH_MAX_CONCURRENT, isServer ? "app is "
				: "refer app is" + app + " ,service name is " + serviceName
						+ ",key is " + key);
	}

}
