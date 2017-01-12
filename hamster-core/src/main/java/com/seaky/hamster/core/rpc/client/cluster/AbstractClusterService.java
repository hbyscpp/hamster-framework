package com.seaky.hamster.core.rpc.client.cluster;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.client.AbstractClient;
import com.seaky.hamster.core.rpc.client.ClientTransport;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.common.DefaultServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ReadOnlyEndpointConfig;
import com.seaky.hamster.core.rpc.exception.AccessRemoteServerException;
import com.seaky.hamster.core.rpc.exception.RpcTimeoutException;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;

import net.jodah.failsafe.CircuitBreaker;

public abstract class AbstractClusterService<Req, Rsp> implements ClusterService<Req, Rsp> {

  protected AbstractClient<Req, Rsp> client;

  protected ServiceLoadBalancer serviceLoadBalancer;

  private final ServiceContext context;

  protected ServiceContext createServiceContext(final ServiceProviderDescriptor sd) {
    ServiceContext sc = new DefaultServiceContext(ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
    ProtocolRequestHeader newHeader = new ProtocolRequestHeader();
    newHeader.setServiceName(sd.getName());
    newHeader.setApp(sd.getApp());
    newHeader.setVersion(sd.getVersion());
    newHeader.setGroup(sd.getGroup());
    ProtocolRequestBody newbody = new ProtocolRequestBody();
    ProtocolRequestBody body = ServiceContextUtils.getRequestBody(context);
    newbody.setParams(body.getParams());
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);

    newHeader.setReferenceApp(header.getReferenceApp());
    newHeader.setReferenceGroup(header.getReferenceGroup());
    newHeader.setReferenceVersion(header.getReferenceVersion());
    ServiceContextUtils.setProviderConfig(sc, new ReadOnlyEndpointConfig(sd.getConfig()));
    ServiceContextUtils.setServerHost(sc, sd.getHost());
    ServiceContextUtils.setServerPort(sc, sd.getPort());
    ServiceContextUtils.setReferenceConfig(sc, ServiceContextUtils.getReferenceConfig(context));
    ServiceContextUtils.setResponseBody(sc, new ProtocolResponseBody());
    ServiceContextUtils.setResponseHeader(sc, new ProtocolResponseHeader());
    // TODO 调用Trace Exception树
    ServiceContextUtils.setInterceptorExceptionTrace(sc,
        ServiceContextUtils.getInterceptorExceptionTrace(context));
    Attachments att = new Attachments(header.getAttachments());
    // 设置协议版本，取最低者
    newHeader.setAttachments(att);
    ServiceContextUtils.setRequestHeader(sc, newHeader);
    ServiceContextUtils.setRequestBody(sc, newbody);
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
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(sc);
    List<ServiceInterceptor> interceptors = client.getServiceInterceptors(header.getServiceName(),
        header.getReferenceApp(), header.getReferenceVersion(), header.getReferenceGroup(),
        ProcessPhase.CLIENT_CALL_SERVICE_INSTANCE);
    // 链接
    final CircuitBreaker cb = client.getCircuitBreaker(sd.getName(), sd.getApp(), sd.getGroup(),
        sd.getVersion(), sd.getHost(), String.valueOf(sd.getPort()));
    final SettableFuture<Object> result =
        client.getClientInterceptorService().process(sc, transport, executor, interceptors);

    result.addListener(new Runnable() {

      @Override
      public void run() {
        try {
          result.get();
          cb.recordSuccess();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          if (cause != null) {
            if (cause instanceof AccessRemoteServerException
                || cause instanceof RpcTimeoutException) {
              cb.recordFailure(cause);
            }
          }

        }
      }
    }, executor);
    return result;
  }

  public String getServiceName() {
    return ServiceContextUtils.getRequestHeader(context).getServiceName();
  }

}
