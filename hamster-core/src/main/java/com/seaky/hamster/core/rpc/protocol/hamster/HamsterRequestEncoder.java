package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HamsterRequestEncoder extends MessageToByteEncoder<HamsterRequest> {

  @Override
  protected void encode(ChannelHandlerContext ctx, HamsterRequest msg, ByteBuf out)
      throws Exception {
    Short version = (Short) msg.getAttachments().get(Constants.PROTOCOL_VERSION);
    if (version == null || version != 0)
      throw new RuntimeException("not support encode request version: " + version);
    if (version == 0) {
      Serializer ser =
          ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
      byte[] datas = ser.serialize(msg);
      out.writeInt(datas.length + 2);
      out.writeShort(version);
      out.writeBytes(datas);
    }

  }
}
