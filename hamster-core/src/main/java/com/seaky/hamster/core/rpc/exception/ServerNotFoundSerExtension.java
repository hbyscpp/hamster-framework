package com.seaky.hamster.core.rpc.exception;

@SuppressWarnings("serial")
public class ServerNotFoundSerExtension extends RpcException {

  public ServerNotFoundSerExtension(String msg) {
    super(RpcException.SERVER_NOT_FOUND_SER_EXT, "server not found ser extension " + msg);
  }

}
