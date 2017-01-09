package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class RpcTimeoutException extends RpcException {


  public RpcTimeoutException(String cause) {
    super(TIMEOUT, cause);
  }

  public RpcTimeoutException(String addr, String serviceName) {
    super(TIMEOUT,
        "call remote service time out addr is " + addr + " service name is " + serviceName);
  }

}
