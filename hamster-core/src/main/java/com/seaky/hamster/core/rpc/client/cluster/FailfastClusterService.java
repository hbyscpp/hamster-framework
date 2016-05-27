package com.seaky.hamster.core.rpc.client.cluster;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.client.AbstractClient;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.exception.AccessRemoteServerException;
import com.seaky.hamster.core.rpc.exception.NoServiceProviderAvailable;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

public class FailfastClusterService<Req, Rsp> extends AbstractClusterService<Req, Rsp> {

  public FailfastClusterService(AbstractClient<Req, Rsp> client,
      ServiceLoadBalancer serviceLoadBalancer, ServiceContext context) {
    super(client, serviceLoadBalancer, context);
  }

  @Override
  public void process(final List<ServiceProviderDescriptor> sds, final Executor executor,
      final SettableFuture<Object> result) {

    ServiceProviderDescriptor sd = choose(sds);
    if (sd == null) {
      result.setException(new NoServiceProviderAvailable(super.getServiceName()));
      return;
    }
    final SettableFuture<Object> resultFuture = invokeService(sd, executor);
    resultFuture.addListener(new Runnable() {

      @Override
      public void run() {
        try {
          Object obj = resultFuture.get();
          result.set(obj);
          return;
        } catch (InterruptedException e) {
          result.setException(e);
          Thread.currentThread().interrupt();
          return;
        } catch (ExecutionException e) {
          Throwable inner = e.getCause();
          if (inner instanceof AccessRemoteServerException) {
            // 访问远程错误,未发送请求
            process(sds, executor, result);
          } else {
            result.setException(inner);
          }

        }
      }
    }, executor);
  }

}
