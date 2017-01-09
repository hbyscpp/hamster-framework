package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServiceProviderNotFoundException extends RpcException {

  public ServiceProviderNotFoundException(String msg) {
    super(SERVICE_PROVIDER_NOT_FOUNT, msg);
  }



}
