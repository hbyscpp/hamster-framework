package com.seaky.hamster.core.rpc.client.loadbalancer;

import java.util.List;
import java.util.Random;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

@SPI("weight_random")
public class WeightRandomServiceLoadBalancer implements ServiceLoadBalancer {

  private static Random rnd = new Random();

  // 采用轮盘赌算法
  @Override
  public ServiceProviderDescriptor choose(List<ServiceProviderDescriptor> sds, ServiceContext sc) {
    if (sds == null || sds.size() == 0)
      return null;

    int total = totalWeight(sds);
    if (total <= 0)
      return null;
    return chooseWeight(sds, total);
  }

  private ServiceProviderDescriptor chooseWeight(List<ServiceProviderDescriptor> sds, int i) {
    if (i > 0 && sds.size() == 1) {
      return sds.get(0);
    }
    int rdata = rnd.nextInt(i) + 1;

    int total = 0;
    for (ServiceProviderDescriptor sd : sds) {
      int weight = sd.getConfig().getValueAsInt(ConfigConstans.PROVIDER_WEIGHT, 1);
      if (weight > 0) {
        int old = total;
        total += weight;
        if (old < rdata && rdata <= total) {
          return sd;
        }
      }
    }

    return null;
  }

  private int totalWeight(List<ServiceProviderDescriptor> sds) {
    int total = 0;
    for (ServiceProviderDescriptor sd : sds) {

      int weight = sd.getConfig().getValueAsInt(ConfigConstans.PROVIDER_WEIGHT, 1);

      if (weight > 0) {
        total += weight;
      }
    }
    return total;
  }
}
