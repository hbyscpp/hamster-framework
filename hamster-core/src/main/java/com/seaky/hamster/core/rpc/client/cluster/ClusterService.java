package com.seaky.hamster.core.rpc.client.cluster;

import java.util.List;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

public interface ClusterService<Req, Rsp> {

	void process(final List<ServiceProviderDescriptor> sds,
			final Executor executor,final SettableFuture<Object> result);

}
