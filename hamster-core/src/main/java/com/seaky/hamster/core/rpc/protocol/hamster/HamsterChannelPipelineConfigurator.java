package com.seaky.hamster.core.rpc.protocol.hamster;

import io.netty.channel.ChannelPipeline;

import com.seaky.hamster.core.rpc.server.NettyServerHandler;
import com.seaky.hamster.core.rpc.server.NettyServerTransport;
import com.seaky.hamster.core.rpc.server.ServerChannelPipelineConfigurator;

public class HamsterChannelPipelineConfigurator implements
		ServerChannelPipelineConfigurator<HamsterRequest, HamsterResponse,NettyServerTransport<HamsterRequest,HamsterResponse>> {

	@Override
	public void config(NettyServerTransport<HamsterRequest, HamsterResponse> transport,
			ChannelPipeline pipeline) {
		pipeline.addLast(new HamsterRequestDecoder());
		pipeline.addLast(new HamsterResponseEncoder());
		pipeline.addLast(new NettyServerHandler<HamsterRequest, HamsterResponse>(transport));
	}

}
