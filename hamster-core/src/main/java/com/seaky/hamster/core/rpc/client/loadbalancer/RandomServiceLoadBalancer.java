package com.seaky.hamster.core.rpc.client.loadbalancer;

import java.util.List;
import java.util.Random;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ReferenceInfo;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

@SPI("random")
public class RandomServiceLoadBalancer implements ServiceLoadBalancer {

	@Override
	public ServiceProviderDescriptor choose(List<ServiceProviderDescriptor> sds,
			EndpointConfig referConfig, ReferenceInfo info, Object[] params) {
		if (sds == null || sds.size() == 0)
			return null;
		if (sds.size() == 1)
			return sds.get(0);
		return sds.get(new Random().nextInt(sds.size()));
	}

}
