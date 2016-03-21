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
import com.seaky.hamster.core.rpc.exception.RpcTimeoutException;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

import io.netty.util.concurrent.ImmediateEventExecutor;

public class FailoverClusterService<Req, Rsp> extends AbstractClusterService<Req, Rsp> {

  public FailoverClusterService(AbstractClient<Req, Rsp> client,
      ServiceLoadBalancer serviceLoadBalancer, ServiceContext context) {
    super(client, serviceLoadBalancer, context);
  }


  public void process(final List<ServiceProviderDescriptor> sds, final Executor executor,
      final SettableFuture<Object> result) {
    ServiceProviderDescriptor sd = choose(sds);
    if (sd == null) {
      // 没找到合适的实例，直接设置
      result.setException(new NoServiceProviderAvailable(super.getServiceName()));
      return;
    }
    // 调用远程服务
    final SettableFuture<Object> resultFuture = invokeService(sd, executor);

    resultFuture.addListener(new Runnable() {

      @Override
      public void run() {
        try {
          Object obj = resultFuture.get();
          result.set(obj);
          return;
        } catch (InterruptedException e) {
          process(sds, executor, result);
          return;
        } catch (ExecutionException e) {
          Throwable inner = e.getCause();
          if (inner instanceof AccessRemoteServerException) {
            // 访问远程错误,未发送请求
            process(sds, executor, result);
          } else if (inner instanceof RpcTimeoutException) {
            // 远程 访问超时，成功发送了请求，但是超时了，服务端可能已经处理了
            process(sds, executor, result);
          } else {
            // 服务端处理时出现的异常
            result.setException(inner);
          }

        }
      }
    }, ImmediateEventExecutor.INSTANCE);
  }

}
