package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

//TODO 优化避免创建datas
public class HamsterRequestEncoder extends MessageToByteEncoder<HamsterRequest> {

	@Override
	protected void encode(ChannelHandlerContext ctx, HamsterRequest msg,
			ByteBuf out) throws Exception {
		Serializer ser = SerializerFactory.getSerializer(Constants.KRYO_SERIAL);
		byte[] datas = ser.serialize(msg);
		out.writeInt(datas.length);
		out.writeBytes(datas);
	}
}
