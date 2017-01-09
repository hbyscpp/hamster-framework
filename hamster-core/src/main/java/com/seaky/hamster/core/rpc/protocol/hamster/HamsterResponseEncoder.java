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
    Short version = (Short) msg.getAttachments().get(Constants.PROTOCOL_VERSION);
    if (version == null || version != 0)
      throw new RuntimeException("not support response version :" + version);
    if (version == 0) {
      Serializer ser =
          ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
      byte[] data = ser.serialize(msg);
      out.writeInt(data.length + 2);
      out.writeShort(version);
      out.writeBytes(data);
    }
  }

}
