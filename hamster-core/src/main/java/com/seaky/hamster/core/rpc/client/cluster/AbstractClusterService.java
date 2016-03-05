package com.seaky.hamster.core.rpc.client.cluster;

import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.client.AbstractClient;
import com.seaky.hamster.core.rpc.client.ClientTransport;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.ErrorAccessRemoteServerException;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ReferenceInfo;
import com.seaky.hamster.core.rpc.protocol.RequestInfo;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.ServiceContext;

public abstract class AbstractClusterService<Req, Rsp> implements
		ClusterService<Req, Rsp> {

	protected AbstractClient<Req, Rsp> client;

	protected ServiceLoadBalancer serviceLoadBalancer;

	private final ServiceContext context;

	private final ReferenceInfo referInfo;
	private static Logger logger = LoggerFactory
			.getLogger(AbstractClusterService.class);

	protected ServiceContext createServiceContext(final ServiceProviderDescriptor sd) {
		RequestInfo info = new RequestInfo(sd.getApp(), context
				.getRequestInfo().getReferApp(), context.getServiceName(),
				sd.getVersion(), context.getReferVersion(), sd.getGroup(),
				context.getRequestInfo().getReferGroup(), context
						.getRequestInfo().getParams());
		info.addAttachment(Constants.SEQ_NUM_KEY,
				String.valueOf(client.getAndIncrementSeqNum()));
		ServiceContext sc = new ServiceContext(info, context.getReferConfig(),
				sd.getConfig(), sd.getHost(), sd.getPort());
		return sc;
	}

	public AbstractClusterService(AbstractClient<Req, Rsp> client,
			ServiceLoadBalancer serviceLoadBalancer, ServiceContext context,
			ReferenceInfo info) {
		this.client = client;
		this.serviceLoadBalancer = serviceLoadBalancer;
		this.context = context;
		this.referInfo = info;
	}

	// 选择ServiceDescriptor
	protected ServiceProviderDescriptor choose(final List<ServiceProviderDescriptor> sds) {
		if (sds == null || sds.size() == 0)
			return null;
		ServiceProviderDescriptor sd = serviceLoadBalancer.choose(sds, context
				.getReferConfig(), referInfo, context.getRequestInfo()
				.getParams());
		if (sd != null)
			// 已经选中的要删除
			sds.remove(sd);
		return sd;
	}

	protected SettableFuture<Object> invokeService(ServiceProviderDescriptor sd,
			final Executor executor) {
		final SettableFuture<Object> result = SettableFuture.create();
		try {
			// 创建context
			final ServiceContext sc = createServiceContext(sd);
			// 获取transport
			final ClientTransport<Req, Rsp> transport = client.getTransport(sd);
			List<ServiceInterceptor> interceptors=client.getServiceInterceptors(sc.getServiceName(),
					sc.getReferApp(), sc.getReferVersion(), sc.getRefergroup(),
					ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
			// 链接
			return client.getClientInterceptorService().process(sc, transport,
					executor,interceptors);

		} catch (Exception e) {
			// 这是未处理的异常
			logger.error("error access remote server", e);
			ErrorAccessRemoteServerException e1 = new ErrorAccessRemoteServerException(
					sd.getName(), Utils.generateKey(sd.getHost(),
							String.valueOf(sd.getPort())), e);
			result.setException(e1);
		}

		return result;

	}

	protected String getServiceName() {
		return context.getServiceName();
	}
}
