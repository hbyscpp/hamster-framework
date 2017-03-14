package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.client.ClientChannelPipelineConfigurator;
import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.client.Netty4ClientHandler;
import com.seaky.hamster.core.rpc.client.NettyClientTransport;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class HamsterClientChannelPipelineConfigurator implements
    ClientChannelPipelineConfigurator<HamsterRequest, HamsterResponse, NettyClientTransport<HamsterRequest, HamsterResponse>> {

  @SuppressWarnings("unchecked")
  @Override
  public void config(NettyClientTransport<HamsterRequest, HamsterResponse> transport,
      ChannelPipeline pipeline, ClientConfig config) {
    pipeline.addLast(new HamsterRequestEncoder());
    pipeline.addLast(new HamsterResponseDecoder());
    pipeline
        .addLast(new IdleStateHandler(config.getHeartbeatIdleTime() + config.getReadTimeout() + 1,
            config.getHeartbeatIdleTime() / 2, 0));
    pipeline.addLast(new Netty4ClientHandler<HamsterRequest, HamsterResponse>(transport,
        ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension("hamster")));

  }


}
