package com.seaky.hamster.core.rpc.protocol.hamster;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.netty4.ByteBufToObjectDecoder;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

public class HamsterResponseDecoder extends
		ByteBufToObjectDecoder<HamsterResponse> {


	public HamsterResponseDecoder() {
		super(ByteOrder.BIG_ENDIAN, 1024 * 1024 * 10, 0, 4, 0, 4, true);

	}

	@Override
	protected HamsterResponse decode(ByteBuf bytebuf) {
		int allBytelength = bytebuf.readableBytes();
		byte[] allBytes = new byte[allBytelength];
		bytebuf.readBytes(allBytes);
		Serializer ser = SerializerFactory
				.getSerializer(Constants.KRYO_SERIAL);
		HamsterResponse response = ser.deSerialize(allBytes,
				HamsterResponse.class);
		return response;
	}

}
