package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.server.NettyServerHandler;
import com.seaky.hamster.core.rpc.server.NettyServerTransport;
import com.seaky.hamster.core.rpc.server.ServerChannelPipelineConfigurator;
import com.seaky.hamster.core.rpc.server.ServerConfig;
import com.seaky.hamster.core.rpc.server.ServerResourceManager;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class HamsterChannelPipelineConfigurator implements
    ServerChannelPipelineConfigurator<HamsterRequest, HamsterResponse, NettyServerTransport<HamsterRequest, HamsterResponse>> {

  @Override
  public void config(NettyServerTransport<HamsterRequest, HamsterResponse> transport,
      ChannelPipeline pipeline, ServerConfig config) {
    pipeline.addLast(new HamsterRequestDecoder());
    pipeline.addLast(new HamsterResponseEncoder());
    pipeline.addLast(new IdleStateHandler(config.getIdleCloseTime(), 0, 0));
    pipeline.addLast(ServerResourceManager.getDispatcherThreadPool(),
        new NettyServerHandler<HamsterRequest, HamsterResponse>(transport));
  }

}
