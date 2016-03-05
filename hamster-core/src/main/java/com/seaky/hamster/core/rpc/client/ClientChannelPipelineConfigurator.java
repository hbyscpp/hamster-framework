package com.seaky.hamster.core.rpc.client;

import io.netty.channel.ChannelPipeline;


public interface ClientChannelPipelineConfigurator<Req, Rsp,T extends ClientTransport<Req, Rsp>> {

	void config(T transport, ChannelPipeline pipeline);

}
