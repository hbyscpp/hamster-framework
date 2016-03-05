package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.client.NettyClient;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;

@SuppressWarnings("rawtypes")
public class HamsterClient extends NettyClient<HamsterRequest, HamsterResponse> {

	private static ExtensionLoader<ProtocolExtensionFactory> ext = ExtensionLoader
			.getExtensionLoaders(ProtocolExtensionFactory.class);

	@SuppressWarnings("unchecked")
	public HamsterClient() {
		super(ext.findExtension("hamster"), configurator);
	}

	private static HamsterClientChannelPipelineConfigurator configurator = new HamsterClientChannelPipelineConfigurator();

}
