package com.seaky.hamster.core.rpc.client.cluster;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.client.AbstractClient;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.common.ServiceContext;

@SPI("failfast")
public interface ClusterServiceFactory {

  <Req, Rsp> ClusterService<Req, Rsp> createService(AbstractClient<Req, Rsp> client,
      ServiceLoadBalancer serviceLoadBalancer, ServiceContext context);
}
