package com.seaky.hamster.core.rpc.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.service.ServiceContext;

public class NettyServerTransport<Req, Rsp> implements
		ServerTransport<Req, Rsp> {

	private Channel channel;

	private Netty4Server<Req, Rsp> server;

	private static Logger logger = LoggerFactory
			.getLogger(NettyServerTransport.class);

	public ProtocolExtensionFactory<Req, Rsp> getProtocolExtensionFactory() {
		return server.getProtocolExtensionFactory();
	}

	public Netty4Server<Req, Rsp> getServer() {
		return server;
	}

	public NettyServerTransport(Channel channel, Netty4Server<Req, Rsp> server) {
		this.channel = channel;
		this.server = server;
	}

	@Override
	public void send(Rsp rsp, final ServiceContext context) {
		ChannelFuture f = channel.writeAndFlush(rsp);
		f.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				if (future.isSuccess()) {
					logger.debug(
							"send response success. server addr:{}:{},app: {},service name: {},version: {},client addr: {}:{},referApp:{},refer version:{}",
							context.getServerHost(), context.getServerPort(),
							context.getServiceName(), context.getApp(),
							context.getServiceVersion(),
							context.getClientHost(), context.getClientPort(),
							context.getReferApp(), context.getReferVersion());
					return;
				}
				if (future.cause() != null) {
					logger.error(
							"send response failure. server addr:{}:{},app: {},service name: {},version: {},client addr: {}:{},referApp:{},refer version:{}",
							context.getServerHost(), context.getServerPort(),
							context.getServiceName(), context.getApp(),
							context.getServiceVersion(),
							context.getClientHost(), context.getClientPort(),
							context.getReferApp(), context.getReferVersion(),future.cause());

				}
				
				if(future.isCancelled())
				{

					logger.error(
							"send response cancel. server addr:{}:{},app: {},service name: {},version: {},client addr: {}:{},referApp:{},refer version:{}",
							context.getServerHost(), context.getServerPort(),
							context.getServiceName(), context.getApp(),
							context.getServiceVersion(),
							context.getClientHost(), context.getClientPort(),
							context.getReferApp(), context.getReferVersion(),future.cause());

				
				}
			}
		});
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) channel.localAddress();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) channel.remoteAddress();
	}

}
