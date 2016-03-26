package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.server.Netty4Server;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

public class HamsterServer extends Netty4Server<HamsterRequest, HamsterResponse> {
  private static HamsterChannelPipelineConfigurator hamsterChannelPipelineConfigurator =
      new HamsterChannelPipelineConfigurator();

  @SuppressWarnings("unchecked")
  public HamsterServer() {
    super(hamsterChannelPipelineConfigurator,
        ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension("hamster"));
  }

}
