package com.seaky.hamster.core.rpc.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;

import com.gs.collections.impl.list.mutable.FastList;
import com.seaky.hamster.core.rpc.exception.MismatchProtocolException;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;

public class Netty4ClientHandler<Req, Rsp> extends ChannelInboundHandlerAdapter {

	private NettyClientTransport<Req, Rsp> transport;

	private ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

	public Netty4ClientHandler(NettyClientTransport<Req, Rsp> client,
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
		this.transport = client;
		this.protocolExtensionFactory = protocolExtensionFactory;
	}

	private List<Rsp> allMsgs = FastList.newList(256);

	private int maxlength = 0;

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (protocolExtensionFactory.getRspClass().isAssignableFrom(
				msg.getClass())) {
			allMsgs.add((Rsp) msg);
		} else {
			throw new MismatchProtocolException(
					protocolExtensionFactory.getRspClass(), msg.getClass(),
					transport.getRemoteAddress(), transport.getLocalAddress());
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		maxlength = maxlength > allMsgs.size() ? maxlength : allMsgs.size();
		transport.setResponse(allMsgs);
		if (allMsgs.size() > 0)
			allMsgs = FastList.newList(maxlength);
		ctx.fireChannelReadComplete();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		transport.close();
		ctx.fireChannelUnregistered();
	}
}
