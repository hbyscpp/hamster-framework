package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class UnknownException extends RpcException {

  public UnknownException(Throwable e) {
    super(UNKNOWN_EXCEPTION, e);
  }

}
