package com.seaky.hamster.core.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.MismatchProtocolException;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

// channel在connection之间不能共享
public class NettyServerHandler<Req, Rsp> extends ChannelInboundHandlerAdapter {

  private static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

  private NettyServerTransport<Req, Rsp> transport;

  private Rsp heartBeatRsp = null;

  public NettyServerHandler(NettyServerTransport<Req, Rsp> transport) {
    this.transport = transport;
    heartBeatRsp = transport.getServer().getProtocolExtensionFactory().createHeartbeatResponse();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    if (transport.getServer().getProtocolExtensionFactory().getReqClass()
        .isAssignableFrom(msg.getClass())) {

      RequestConvertor<Req> reqAttr =
          transport.getServer().getProtocolExtensionFactory().getRequestConvertor();
      ProtocolRequestHeader header = reqAttr.parseProtocolHeader((Req) msg);
      Byte type = header.getAttachments().getAsByte(Constants.MSG_TYPE);
      if (type != null && Constants.MSG_HEARTBEAT_TYPE == type) {
        // 发送心跳响应
        ctx.writeAndFlush(heartBeatRsp).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
      } else {
        transport.getServer().getRequestDispatcher().dispatchMessage((Req) msg, header, transport);
      }
    } else {
      // 协议解析出现问题,抛出异常，关闭连接
      throw new MismatchProtocolException(
          transport.getServer().getProtocolExtensionFactory().getReqClass(), msg.getClass(),
          transport.getLocalAddress(), transport.getRemoteAddress());
    }
  }



  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.error("", cause);
    ctx.close();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    if (evt instanceof IdleStateEvent) {
      IdleState state = ((IdleStateEvent) evt).state();
      if (state == IdleState.READER_IDLE) {
        // 读超时
        logger.warn("", "read timeout");
        ctx.close();
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }

  }

}
