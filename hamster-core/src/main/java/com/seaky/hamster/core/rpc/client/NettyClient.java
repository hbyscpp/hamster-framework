package com.seaky.hamster.core.rpc.client;

import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

public class NettyClient<Req, Rsp> extends AbstractClient<Req, Rsp> {

	private ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> configurator;

	protected NettyClient(
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory,
			ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> configurator) {
		super(protocolExtensionFactory);
		this.configurator = configurator;
	}

	@Override
	protected ClientTransport<Req, Rsp> createTransport(ServiceProviderDescriptor sd,
			ClientConfig config,
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory,AbstractClient<Req, Rsp> client) {
		NettyClientTransport<Req, Rsp> transport = new NettyClientTransport<Req, Rsp>(
				sd, config, configurator, protocolExtensionFactory,client);
		return transport;
	}

	
}
