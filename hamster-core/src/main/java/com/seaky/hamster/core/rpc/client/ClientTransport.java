package com.seaky.hamster.core.rpc.client;

import java.net.InetSocketAddress;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.common.ServiceContext;

public interface ClientTransport<Req, Rsp> {

  SettableFuture<Void> connect();

  SettableFuture<Rsp> send(Req req, ServiceContext sc);

  InetSocketAddress getLocalAddress();

  InetSocketAddress getRemoteAddress();

  // 只有连接状态返回true
  boolean isConnected();

  // 正在连接的状态
  boolean isClosed();

  void close();
}
