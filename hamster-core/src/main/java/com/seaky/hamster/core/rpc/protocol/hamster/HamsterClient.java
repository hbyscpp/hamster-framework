package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.client.NettyClient;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

public class HamsterClient extends NettyClient<HamsterRequest, HamsterResponse> {

  @SuppressWarnings("unchecked")
  public HamsterClient() {
    super(ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension("hamster"),
        configurator);
  }

  private static HamsterClientChannelPipelineConfigurator configurator =
      new HamsterClientChannelPipelineConfigurator();

}
