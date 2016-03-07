package com.seaky.hamster.core.rpc.client.router;

import java.util.List;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

/**
 * 
 * 从一组服务中挑选出适合的服务集群
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
@SPI("default")
public interface ServiceRouter {
  List<ServiceProviderDescriptor> choose(List<ServiceProviderDescriptor> allService,
      ServiceContext context);
}
