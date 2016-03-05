package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.server.Netty4Server;

public class HamsterServer extends
		Netty4Server<HamsterRequest, HamsterResponse> {
	@SuppressWarnings("rawtypes")
	private static ExtensionLoader<ProtocolExtensionFactory> ext = ExtensionLoader
			.getExtensionLoaders(ProtocolExtensionFactory.class);
	private static HamsterChannelPipelineConfigurator hamsterChannelPipelineConfigurator = new HamsterChannelPipelineConfigurator();

	@SuppressWarnings("unchecked")
	public HamsterServer() {
		super(hamsterChannelPipelineConfigurator,
				ext.findExtension("hamster"));
	}

}
