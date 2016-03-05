package com.seaky.hamster.core.rpc.server;

import java.net.InetSocketAddress;

import com.seaky.hamster.core.service.ServiceContext;

public interface ServerTransport<Req,Rsp> {

	void send(Rsp rsp,ServiceContext context);
	//本地服务的地址
	InetSocketAddress getLocalAddress();
	//远程client的地址
	InetSocketAddress getRemoteAddress();
}
