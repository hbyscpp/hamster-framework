package com.seaky.hamster.core.rpc.netty4;

import java.nio.ByteOrder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
public abstract class ByteBufToObjectDecoder<T> extends
		LengthFieldBasedFrameDecoder {

	public ByteBufToObjectDecoder(ByteOrder byteOrder, int maxFrameLength,
			int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
			int initialBytesToStrip, boolean failFast) {
		super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength,
				lengthAdjustment, initialBytesToStrip, failFast);
	}

	@Override
	protected T decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		if (frame == null) {
			return null;
		}
		T t = decode(frame);
		return t;
	}

	protected abstract T decode(ByteBuf bytebuf);

	@Override
	protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer,
			int index, int length) {
		return buffer.slice(index, length);
	}

}
