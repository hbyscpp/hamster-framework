package com.seaky.hamster.core.rpc.client.cluster;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.client.AbstractClient;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.common.ServiceContext;

@SPI("failfast")
public class FailfastClusterServiceFactory implements ClusterServiceFactory {

  @Override
  public <Req, Rsp> ClusterService<Req, Rsp> createService(AbstractClient<Req, Rsp> client,
      ServiceLoadBalancer serviceLoadBalancer, ServiceContext context) {
    return new FailfastClusterService<Req, Rsp>(client, serviceLoadBalancer, context);
  }

}
