package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServiceProviderConfigNotFoundException extends RpcException {

  public ServiceProviderConfigNotFoundException(String app, String serviceName, String version, String group) {
    super(SERVICE_PROVIDER_CONFIG_NOT_FOUND, "app is " + app + " ,service name is " + serviceName
        + ",version is " + version + ",group is " + group);
  }

}
