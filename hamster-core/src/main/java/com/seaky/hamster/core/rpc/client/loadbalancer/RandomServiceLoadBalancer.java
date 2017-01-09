package com.seaky.hamster.core.rpc.client.loadbalancer;

import java.util.List;
import java.util.Random;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

@SPI("random")
public class RandomServiceLoadBalancer implements ServiceLoadBalancer {

  private static Random rnd = new Random();

  @Override
  public ServiceProviderDescriptor choose(List<ServiceProviderDescriptor> sds, ServiceContext sc) {
    if (sds == null || sds.size() == 0)
      return null;
    if (sds.size() == 1)
      return sds.get(0);
    return sds.get(rnd.nextInt(sds.size()));
  }

}
