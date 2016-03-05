package com.seaky.hamster.core.rpc.server;


import io.netty.channel.ChannelPipeline;

/**
 * 
 * 配置netty server 的ChannelHandler
 * 
 * @author seaky
 * @version
 * @since 1.0.0
 */
public interface ServerChannelPipelineConfigurator<Req,Rsp,T extends ServerTransport<Req, Rsp>> {
	
	void config(T transport,ChannelPipeline pipeline);

}
