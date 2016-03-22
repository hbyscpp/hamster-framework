package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class UnknownException extends RpcException {

  public UnknownException(String msg) {
    super(UNKONOW_EXCEPTION, msg);
  }

}
