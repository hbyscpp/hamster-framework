package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HamsterResponseEncoder extends MessageToByteEncoder<HamsterResponse> {


  @Override
  protected void encode(ChannelHandlerContext ctx, HamsterResponse msg, ByteBuf out)
      throws Exception {
    Byte msgType = (Byte) msg.getAttachments().get(Constants.MSG_TYPE);
    if (msgType == null) {
      msgType = Constants.MSG_NORMAL_TYPE;
    }
    if (msgType != Constants.MSG_NORMAL_TYPE) {
      // 心跳消息
      out.writeInt(1);
      out.writeByte(msgType);
      return;
    }
    Serializer ser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
    byte[] data = ser.serialize(msg);
    out.writeInt(data.length + 2);
    out.writeByte(msgType);
    out.writeByte(0);
    out.writeBytes(data);
  }

}
