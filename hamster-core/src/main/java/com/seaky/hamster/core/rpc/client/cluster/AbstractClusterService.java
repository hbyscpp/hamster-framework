package com.seaky.hamster.core.rpc.client.cluster;

import java.util.List;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.client.AbstractClient;
import com.seaky.hamster.core.rpc.client.ClientTransport;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.common.DefaultServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ReadOnlyEndpointConfig;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

public abstract class AbstractClusterService<Req, Rsp> implements ClusterService<Req, Rsp> {

  protected AbstractClient<Req, Rsp> client;

  protected ServiceLoadBalancer serviceLoadBalancer;

  private final ServiceContext context;

  protected ServiceContext createServiceContext(final ServiceProviderDescriptor sd) {
    ServiceContext sc = new DefaultServiceContext(ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
    ServiceContextUtils.setServiceName(sc, sd.getName());
    ServiceContextUtils.setApp(sc, sd.getApp());
    ServiceContextUtils.setVersion(sc, sd.getVersion());
    ServiceContextUtils.setGroup(sc, sd.getGroup());
    ServiceContextUtils.setProviderConfig(sc, new ReadOnlyEndpointConfig(sd.getConfig()));
    ServiceContextUtils.setRequestParams(sc, ServiceContextUtils.getRequestParams(context));
    ServiceContextUtils.setServerHost(sc, sd.getHost());
    ServiceContextUtils.setServerPort(sc, sd.getPort());


    ServiceContextUtils.setReferenceApp(sc, ServiceContextUtils.getReferenceApp(context));
    ServiceContextUtils.setReferenceVersion(sc, ServiceContextUtils.getReferenceVersion(context));
    ServiceContextUtils.setReferenceGroup(sc, ServiceContextUtils.getReferenceGroup(context));
    ServiceContextUtils.setReferenceConfig(sc, ServiceContextUtils.getReferenceConfig(context));
    ServiceContextUtils.setResponse(sc, new Response());
    ServiceContextUtils.setInterceptorExceptionTrace(sc,
        ServiceContextUtils.getInterceptorExceptionTrace(context));
    // TODO attachments 传递
    Attachments attachments = new Attachments();


    attachments.addAttachment(Constants.SEQ_NUM_KEY,
        String.valueOf(client.getAndIncrementSeqNum()));
    sc.setAttribute(ServiceContext.REQUEST_ATTACHMENTS, attachments);

    return sc;
  }

  public AbstractClusterService(AbstractClient<Req, Rsp> client,
      ServiceLoadBalancer serviceLoadBalancer, ServiceContext context) {
    this.client = client;
    this.serviceLoadBalancer = serviceLoadBalancer;
    this.context = context;
  }

  // 选择ServiceDescriptor
  protected ServiceProviderDescriptor choose(final List<ServiceProviderDescriptor> sds) {
    if (sds == null || sds.size() == 0)
      return null;
    ServiceProviderDescriptor sd = serviceLoadBalancer.choose(sds, context);
    if (sd != null)
      // 已经选中的要删除
      sds.remove(sd);
    return sd;
  }

  protected SettableFuture<Object> invokeService(ServiceProviderDescriptor sd,
      final Executor executor) {
    // 创建context
    final ServiceContext sc = createServiceContext(sd);
    // 获取transport
    final ClientTransport<Req, Rsp> transport = client.getTransport(sd);
    List<ServiceInterceptor> interceptors =
        client.getServiceInterceptors(ServiceContextUtils.getServiceName(sc),
            ServiceContextUtils.getReferenceApp(sc), ServiceContextUtils.getReferenceVersion(sc),
            ServiceContextUtils.getReferenceGroup(sc), ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
    // 链接
    return client.getClientInterceptorService().process(sc, transport, executor, interceptors);

  }

  protected String getServiceName() {
    return context.getAttribute(ServiceContext.SERVICENAME, String.class);
  }
}
