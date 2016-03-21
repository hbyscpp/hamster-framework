package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServiceReferenceConfigNotFoundException extends RpcException {

	public ServiceReferenceConfigNotFoundException(String app, String serviceName,String version,
			String group) {
		super(SERVICE_REFERENCE_CONFIG_NOT_FOUND, "referApp is " + app + " ,service name is "
				+ serviceName + ",version is " + version+",group is "+group);
	}

}
