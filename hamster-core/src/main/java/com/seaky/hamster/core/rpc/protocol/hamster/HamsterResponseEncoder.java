package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HamsterResponseEncoder extends
		MessageToByteEncoder<HamsterResponse> {


	@Override
	protected void encode(ChannelHandlerContext ctx, HamsterResponse msg,
			ByteBuf out) throws Exception {
		Serializer ser = SerializerFactory.getSerializer(Constants.KRYO_SERIAL);
		byte[] data=ser.serialize(msg);
		out.writeInt(data.length);
		out.writeBytes(data);
	}

}
