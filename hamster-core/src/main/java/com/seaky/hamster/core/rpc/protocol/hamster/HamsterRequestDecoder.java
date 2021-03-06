package com.seaky.hamster.core.rpc.protocol.hamster;

import java.nio.ByteOrder;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.netty4.ByteBufToObjectDecoder;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.buffer.ByteBuf;

public class HamsterRequestDecoder extends ByteBufToObjectDecoder<HamsterRequest> {

  public HamsterRequestDecoder() {
    super(ByteOrder.BIG_ENDIAN, 1024 * 1024 * 10, 0, 4, 0, 4, true);
  }

  @Override
  protected HamsterRequest decode(ByteBuf bytebuf) {

    byte msgtype = bytebuf.readByte();
    if (msgtype != Constants.MSG_NORMAL_TYPE) {

      HamsterRequest req = new HamsterRequest();
      req.addAttachment(Constants.MSG_TYPE, msgtype);
      return req;
    }
    byte version = bytebuf.readByte();
    if (version == 0) {
      int allBytelength = bytebuf.readableBytes();
      byte[] allBytes = new byte[allBytelength];
      bytebuf.readBytes(allBytes);
      Serializer ser =
          ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
      HamsterRequest request = ser.deSerialize(allBytes, HamsterRequest.class);
      return request;
    }
    throw new RuntimeException("not support version " + version);
  }



}
