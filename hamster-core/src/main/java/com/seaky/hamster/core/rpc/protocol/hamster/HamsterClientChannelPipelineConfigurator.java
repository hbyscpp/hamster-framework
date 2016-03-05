package com.seaky.hamster.core.rpc.protocol.hamster;

import io.netty.channel.ChannelPipeline;

import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.client.ClientChannelPipelineConfigurator;
import com.seaky.hamster.core.rpc.client.Netty4ClientHandler;
import com.seaky.hamster.core.rpc.client.NettyClientTransport;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;

public class HamsterClientChannelPipelineConfigurator
    implements
    ClientChannelPipelineConfigurator<HamsterRequest, HamsterResponse, NettyClientTransport<HamsterRequest, HamsterResponse>> {
  @SuppressWarnings("rawtypes")
  private static ExtensionLoader<ProtocolExtensionFactory> ext = ExtensionLoader
      .getExtensionLoaders(ProtocolExtensionFactory.class);

  @SuppressWarnings("unchecked")
  @Override
  public void config(NettyClientTransport<HamsterRequest, HamsterResponse> transport,
      ChannelPipeline pipeline) {
    pipeline.addLast(new HamsterRequestEncoder());
    pipeline.addLast(new HamsterResponseDecoder());
    pipeline.addLast(new Netty4ClientHandler<HamsterRequest, HamsterResponse>(transport, ext
        .findExtension("hamster")));

  }


}
