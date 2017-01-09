package com.seaky.hamster.core.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.MismatchProtocolException;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class Netty4ClientHandler<Req, Rsp> extends ChannelInboundHandlerAdapter {

  private NettyClientTransport<Req, Rsp> transport;

  private ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private Req heartBeatReq = null;

  private static Logger logger = LoggerFactory.getLogger(Netty4ClientHandler.class);

  public Netty4ClientHandler(NettyClientTransport<Req, Rsp> client,
      ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
    this.transport = client;
    this.protocolExtensionFactory = protocolExtensionFactory;
    heartBeatReq = protocolExtensionFactory.createHeartbeatRequest();

  }

  private long lastHeartbeatRspTime = 0;

  private long lastHeartbeatSendTime = 0;

  @SuppressWarnings("unchecked")
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (protocolExtensionFactory.getRspClass().isAssignableFrom(msg.getClass())) {

      Rsp rsp = (Rsp) msg;

      ResponseConvertor<Rsp> reqAttr = protocolExtensionFactory.getResponseConvertor();
      ProtocolResponseHeader header = reqAttr.parseHeader(rsp);
      Byte type = header.getAttachments().getAsByte(Constants.MSG_TYPE);
      if (type != null && Constants.MSG_HEARTBEAT_RSP_TYPE == type) {
        // 心跳响应
        lastHeartbeatRspTime = System.currentTimeMillis();
      } else {
        transport.setResponse(rsp, header);
      }
    } else {
      throw new MismatchProtocolException(protocolExtensionFactory.getRspClass(), msg.getClass(),
          transport.getRemoteAddress(), transport.getLocalAddress());
    }
  }



  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {

      IdleState state = ((IdleStateEvent) evt).state();
      if (state == IdleState.WRITER_IDLE) {
        ctx.writeAndFlush(heartBeatReq).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        lastHeartbeatSendTime = System.currentTimeMillis();
      } else if (state == IdleState.READER_IDLE) {
        // 读超时
        logger.warn("time out close last send {},last receive {}", lastHeartbeatSendTime,
            lastHeartbeatRspTime);
        transport.close(true);

      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    transport.close(true);
    super.channelInactive(ctx);
  }
}
