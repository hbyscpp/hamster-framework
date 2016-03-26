package com.seaky.hamster.core.rpc.utils;

import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.client.cluster.ClusterServiceFactory;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.client.router.ServiceRouter;
import com.seaky.hamster.core.rpc.protocol.ExceptionConvertor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.serialization.Serializer;

public class ExtensionLoaderConstants {

  public static ExtensionLoader<Serializer> SERIALIZER_EXTENSION =
      ExtensionLoader.getExtensionLoaders(Serializer.class);
  public static ExtensionLoader<ClusterServiceFactory> CLUSTER_EXTENSION =
      ExtensionLoader.getExtensionLoaders(ClusterServiceFactory.class);

  public static ExtensionLoader<ExceptionConvertor> EXCEPTION_CONVERTOR_EXTENSION =
      ExtensionLoader.getExtensionLoaders(ExceptionConvertor.class);

  @SuppressWarnings("rawtypes")
  public static ExtensionLoader<ProtocolExtensionFactory> PROTOCOLFACTORY_EXTENSION =
      ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class);

  public static ExtensionLoader<ServiceRouter> ROUTER_EXTENSION =
      ExtensionLoader.getExtensionLoaders(ServiceRouter.class);

  public static ExtensionLoader<ServiceLoadBalancer> LOADBALANCE_EXTENSION =
      ExtensionLoader.getExtensionLoaders(ServiceLoadBalancer.class);
}
