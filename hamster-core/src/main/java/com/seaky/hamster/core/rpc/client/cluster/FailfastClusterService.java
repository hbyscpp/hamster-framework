package com.seaky.hamster.core.rpc.client.cluster;

import io.netty.util.concurrent.ImmediateEventExecutor;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.client.AbstractClient;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.exception.CancelConnectToRemoteServerException;
import com.seaky.hamster.core.rpc.exception.ErrorConnectRemoteServerException;
import com.seaky.hamster.core.rpc.exception.ErrorSendToRemoteServerException;
import com.seaky.hamster.core.rpc.exception.NoServiceAvailable;
import com.seaky.hamster.core.rpc.protocol.ReferenceInfo;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.service.ServiceContext;

public class FailfastClusterService<Req, Rsp> extends
		AbstractClusterService<Req, Rsp> {

	public FailfastClusterService(AbstractClient<Req, Rsp> client,
			ServiceLoadBalancer serviceLoadBalancer, ServiceContext context,
			ReferenceInfo info) {
		super(client, serviceLoadBalancer, context, info);
	}

	@Override
	public void process(final List<ServiceProviderDescriptor> sds,
			final Executor executor, final SettableFuture<Object> result) {

		ServiceProviderDescriptor sd = choose(sds);
		if (sd == null) {
			result.setException(new NoServiceAvailable(super.getServiceName()));
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
					if (inner instanceof ErrorConnectRemoteServerException) {
						// 访问远程错误,未发送请求
						process(sds, executor, result);
					} else if (inner instanceof ErrorSendToRemoteServerException) {
						// 访问远程错误,未发送请求
						process(sds, executor, result);
					} else if (inner instanceof CancelConnectToRemoteServerException) {
						process(sds, executor, result);
					} else {
						result.setException(inner);
					}

				}
			}
		}, ImmediateEventExecutor.INSTANCE);
	}

}
