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

  // TODO 避免创建byte[]
  @Override
  protected HamsterRequest decode(ByteBuf bytebuf) {

    int allBytelength = bytebuf.readableBytes();
    byte[] allBytes = new byte[allBytelength];
    bytebuf.readBytes(allBytes);
    Serializer ser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
    HamsterRequest request = ser.deSerialize(allBytes, HamsterRequest.class);
    return request;
  }

}
