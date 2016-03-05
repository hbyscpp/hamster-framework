package com.seaky.hamster.core.rpc.client;

import java.net.InetSocketAddress;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.service.ServiceContext;

public interface ClientTransport<Req,Rsp> {

	SettableFuture<Void> connect(ServiceContext sc);
	
	SettableFuture<Rsp> send(Req req,ServiceContext sc);

	InetSocketAddress getLocalAddress();

	InetSocketAddress getRemoteAddress();

	//只有连接状态返回true
	boolean isConnected();
	
	//正在连接的状态
	boolean isClosed();
	
	void close();
}
