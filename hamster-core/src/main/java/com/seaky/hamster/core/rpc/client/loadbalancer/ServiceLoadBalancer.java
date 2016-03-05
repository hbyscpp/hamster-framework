package com.seaky.hamster.core.rpc.client.loadbalancer;

import java.util.List;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ReferenceInfo;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

@SPI("random")
public interface ServiceLoadBalancer {
	public ServiceProviderDescriptor choose(List<ServiceProviderDescriptor> sds,
			EndpointConfig referConfig, ReferenceInfo referApp,Object[] params);
}
