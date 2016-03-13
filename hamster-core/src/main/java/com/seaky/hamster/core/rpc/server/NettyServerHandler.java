package com.seaky.hamster.core.rpc.server;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.exception.MismatchProtocolException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

//channel在connection之间不能共享
public class NettyServerHandler<Req, Rsp> extends ChannelInboundHandlerAdapter {
	private static Logger logger = LoggerFactory
			.getLogger(NettyServerHandler.class);
	private NettyServerTransport<Req, Rsp> transport;

	public NettyServerHandler(NettyServerTransport<Req, Rsp> transport) {
		this.transport = transport;
	}

	private int maxlength = 256;

	private List<Req> allMsgs = new ArrayList<Req>(256);

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (transport.getServer().getProtocolExtensionFactory().getReqClass()
				.isAssignableFrom(msg.getClass())) {
			allMsgs.add((Req) msg);
		} else {
			// 协议解析出现问题,抛出异常，关闭连接
			throw new MismatchProtocolException(transport.getServer()
					.getProtocolExtensionFactory().getReqClass(),
					msg.getClass(),transport.getLocalAddress(),transport.getRemoteAddress());
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// 保证一次读取最大的消息
		if (allMsgs.size() == 0)
			return;
		maxlength = maxlength > allMsgs.size() ? maxlength : allMsgs.size();
		transport.getServer().getRequestDispatcher()
				.dispatchMessages(allMsgs, transport);
		if (maxlength > 0)
			allMsgs = new ArrayList<Req>(maxlength);
		ctx.fireChannelReadComplete();
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage());
		ctx.close();
	}

}
