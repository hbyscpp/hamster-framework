package com.seaky.hamster.core.rpc.client.router;

import java.util.List;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

// 默认router返回所有的候选service
@SPI("default")
public class DefaultRouter implements ServiceRouter {

  @Override
  public List<ServiceProviderDescriptor> choose(List<ServiceProviderDescriptor> allService,
      ServiceContext context) {
    if (allService == null || allService.size() == 0)
      return null;
    return allService;
  }

}
