package com.seaky.hamster.core.rpc.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.client.cluster.ClusterService;
import com.seaky.hamster.core.rpc.client.cluster.ClusterServiceFactory;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.client.router.ServiceRouter;
import com.seaky.hamster.core.rpc.common.DefaultServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.config.ReadOnlyEndpointConfig;
import com.seaky.hamster.core.rpc.exception.NoRouteServiceProviderException;
import com.seaky.hamster.core.rpc.exception.NoServiceProviderMatchException;
import com.seaky.hamster.core.rpc.exception.ServiceProviderNotFoundException;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.trace.ClientCallExceptionTrace;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;
import com.seaky.hamster.core.rpc.utils.Utils;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;

public class AsynServiceExecutor<Req, Rsp> {

  private AbstractClient<Req, Rsp> client;

  public AsynServiceExecutor(AbstractClient<Req, Rsp> client) {
    this.client = client;
  }

  public void callService(ReferenceServiceRequest request, SettableFuture<Object> result,
      EndpointConfig config) {

    // 1初始化context
    ServiceContext sc = new DefaultServiceContext(ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
    ServiceContextUtils.setServiceName(sc, request.getServiceName());
    ServiceContextUtils.setReferenceApp(sc, request.getReferenceApp());
    ServiceContextUtils.setReferenceVersion(sc, request.getReferenceVersion());
    ServiceContextUtils.setReferenceGroup(sc, request.getReferenceGroup());
    ServiceContextUtils.setRequestParams(sc, request.getParams());
    ServiceContextUtils.setRequestAttachments(sc, new Attachments());
    ServiceContextUtils.setReferenceConfig(sc, new ReadOnlyEndpointConfig(config));
    ServiceContextUtils.setResponse(sc, new Response());
    ServiceContextUtils.setInterceptorExceptionTrace(sc,
        new ClientCallExceptionTrace(client.getAndIncrementTraceId()));
    boolean notUseThreadpoolConfig =
        config.getValueAsBoolean(ConfigConstans.REFERENCE_ASYNPOOL_THREAD_EXE, false);

    if (!notUseThreadpoolConfig) {
      // 异步计算线程之中执行
      EventExecutor executor = ClientResourceManager.getAsynExecutorPool().next();
      CallRemoteServiceTask<Req, Rsp> task =
          new CallRemoteServiceTask<Req, Rsp>(client, sc, result, executor);
      executor.execute(task);
    } else {
      // 线程池执行
      String serviceThreadpoolName = config.get(ConfigConstans.REFERENCE_THREADPOOL_NAME,
          ConfigConstans.REFERENCE_THREADPOOL_NAME_DEFAULT);
      EventExecutorGroup pool = null;

      int maxThread = config.getValueAsInt(ConfigConstans.REFERENCE_THREADPOOL_MAXSIZE,
          ConfigConstans.REFERENCE_THREADPOOL_MAXSIZE_DEFAULT);

      pool = ClientResourceManager.getServiceThreadpoolManager().create(serviceThreadpoolName,
          maxThread);
      EventExecutor executor = pool.next();
      CallRemoteServiceTask<Req, Rsp> task =
          new CallRemoteServiceTask<Req, Rsp>(client, sc, result, executor);
      executor.execute(task);
    }

  }

  // 代表每一个请求的任务
  private static class CallRemoteServiceTask<Req, Rsp> implements Runnable {

    // 客户端最终的结果
    private SettableFuture<Object> finalResult;

    private ServiceContext context;

    private AbstractClient<Req, Rsp> client;

    private EventExecutor executor;


    public CallRemoteServiceTask(AbstractClient<Req, Rsp> client, ServiceContext context,
        SettableFuture<Object> result, EventExecutor executor) {
      this.client = client;
      this.context = context;
      this.finalResult = result;
      this.executor = executor;
    }

    @Override
    public void run() {

      // prProcess 和postProess 保证在同一个线程执行
      // 1执行interceptor
      final List<ServiceInterceptor> interceptors = client.getServiceInterceptors(
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getReferenceVersion(context),
          ServiceContextUtils.getReferenceGroup(context), ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
      final int size = interceptors == null ? 0 : interceptors.size();
      if (size > 0) {
        boolean isDone = client.getClientInterceptorService().preProcess(context, interceptors);
        if (isDone) {
          // 无需发送网络请求
          client.getClientInterceptorService().setFuture(context, finalResult);
          return;
        }
      }
      final SettableFuture<Object> result = SettableFuture.create();
      // 2选择远程服务实例集群，并执行
      List<ServiceProviderDescriptor> sds = null;
      ClusterService<Req, Rsp> clusterService = null;
      ServiceLoadBalancer loadBalancer = null;
      try {
        sds = chooseServiceInstances();
        loadBalancer = getLoadBalancer();
        clusterService = getClusterService(loadBalancer);
      } catch (Exception e) {
        // 执行出现问题
        client.getClientInterceptorService().addException(context,
            ClientCallExceptionTrace.SELECT_CLUSTER_SERVICE_PREPROCESS,
            ServiceContextUtils.getServiceName(context), e);
        postProcess(interceptors, size, e);
        return;
      }
      try {
        clusterService.process(sds, executor, result);
      } catch (Exception e) {
        client.getClientInterceptorService().addException(context,
            ClientCallExceptionTrace.CALL_CLUSTER_SERVICE,
            ServiceContextUtils.getServiceName(context), e);
        postProcess(interceptors, size, e);
        return;

      }
      result.addListener(new Runnable() {
        @Override
        public void run() {
          try {
            // 未发生任何异常
            Object obj = result.get();
            ServiceContextUtils.getResponse(context).setResult(obj);
            postProcess(interceptors, size, null);
            return;
          } catch (InterruptedException e) {
            postProcess(interceptors, size, e);
            return;
          } catch (ExecutionException e) {
            Throwable innerException = e.getCause();
            postProcess(interceptors, size, innerException);
            return;
          }
        }

      }, executor);

    }

    private void postProcess(final List<ServiceInterceptor> receiveInterceptors, final int size,
        Throwable e) {
      if (e != null) {
        ServiceContextUtils.getResponse(context).setResult(e);
      }
      client.getClientInterceptorService().postProcess(context, receiveInterceptors, size);
      client.getClientInterceptorService().setFuture(context, finalResult);
      return;
    }

    private ClusterService<Req, Rsp> getClusterService(ServiceLoadBalancer loadbalancer) {
      String clusterName = ServiceContextUtils.getReferenceConfig(context)
          .get(ConfigConstans.REFERENCE_CLUSTER, ConfigConstans.REFERENCE_CLUSTER_DEFAULT);
      ClusterServiceFactory csf =
          ExtensionLoaderConstants.CLUSTER_EXTENSION.findExtension(clusterName);
      if (csf == null) {
        throw new RuntimeException("no service cluster found,name is " + clusterName);
      }
      // 每个请求创建集群实例对象
      return csf.createService(client, loadbalancer, context);
    }

    private List<ServiceProviderDescriptor> chooseServiceInstances() {
      // 2获取服务的所有实例
      Collection<ServiceProviderDescriptor> allServiceDescriptors =
          client.getAllServices(ServiceContextUtils.getServiceName(context));
      if (allServiceDescriptors == null || allServiceDescriptors.size() == 0) {
        throw new ServiceProviderNotFoundException(ServiceContextUtils.getServiceName(context));
      }
      List<ServiceProviderDescriptor> allSd = new ArrayList<ServiceProviderDescriptor>();
      String addesses = ServiceContextUtils.getReferenceConfig(context)
          .get(ConfigConstans.REFERENCE_SERVICE_PROVIDER_ADDRESSES, null);
      Map<String, Integer> directHostAndPorts = addressToMap(addesses);
      // 3选择有配置的实例
      for (ServiceProviderDescriptor sd : allServiceDescriptors) {
        // 匹配相同参数和查看是否是直接指定连接的方式
        if (compareParam(context, sd)
            && Utils.isVersionComp(ServiceContextUtils.getReferenceVersion(context),
                sd.getVersion())
            && Utils.isGroupMatch(ServiceContextUtils.getReferenceGroup(context), sd.getGroup())
            && client.protocolExtensionFactory.protocolName().equals(sd.getProtocol())) {
          // provider隐藏，客户端只能采用直连的方式
          if (sd.isForceAccess()) {
            // 强制使用该provider
            allSd.clear();
            allSd.add(sd);
            break;
          }
          if (directHostAndPorts != null && directHostAndPorts.size() > 0) {
            // 直连的方式
            if (compareHost(sd, directHostAndPorts)) {
              allSd.add(sd);
            }
          } else {
            // 非直连
            if (!sd.isHidden()) {
              allSd.add(sd);
            }
          }
        }
      }
      if (allSd.size() == 0) {
        throw new NoServiceProviderMatchException(ServiceContextUtils.getServiceName(context));
      }
      // 4 route 从实例中选择符合要求的实例
      String routerName = ServiceContextUtils.getReferenceConfig(context)
          .get(ConfigConstans.REFERENCE_ROUTER, ConfigConstans.REFERENCE_ROUTER_DEFAULT);
      ServiceRouter router = ExtensionLoaderConstants.ROUTER_EXTENSION.findExtension(routerName);
      if (router == null) {
        throw new RuntimeException("no service router found,router name " + routerName);
      }
      // 选出可以提供服务的集群实例
      List<ServiceProviderDescriptor> sds = router.choose(allSd, context);
      if (sds == null || sds.size() == 0)
        throw new NoRouteServiceProviderException(ServiceContextUtils.getServiceName(context));
      return sds;
    }

    private boolean compareHost(ServiceProviderDescriptor sd,
        Map<String, Integer> directHostAndPorts) {
      Integer port = directHostAndPorts.get(sd.getHost());
      if (port == null)
        return false;
      if (port <= 0)
        return true;
      if (sd.getPort() == port.intValue())
        return true;
      return false;
    }

    private Map<String, Integer> addressToMap(String addresses) {
      if (StringUtils.isBlank(addresses)) {
        return null;
      }
      String[] addrs = addresses.split(";");
      if (addrs == null || addrs.length == 0)
        return null;

      Map<String, Integer> addrMap = new HashMap<>();
      for (String addr : addrs) {

        String[] hostAndPort = addr.split(":");

        if (hostAndPort == null || hostAndPort.length != 2) {
          throw new RuntimeException("service provide address error: " + addresses);
        }
        addrMap.put(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
      }
      return addrMap;
    }

    private boolean compareParam(ServiceContext context, ServiceProviderDescriptor sd) {
      EndpointConfig config = ServiceContextUtils.getReferenceConfig(context);
      String returnType = config.get(ConfigConstans.REFERENCE_RETURN);

      if (!StringUtils.equals(returnType, sd.getReturnType()))
        return false;

      Object[] params = ServiceContextUtils.getRequestParams(context);
      return Utils.isMatch(sd.getParamTypes(), params);

    }

    private ServiceLoadBalancer getLoadBalancer() {
      String lbName = ServiceContextUtils.getReferenceConfig(context).get(
          ConfigConstans.REFERENCE_LOADBALANCER, ConfigConstans.REFERENCE_LOADBALANCER_DEFAULT);
      ServiceLoadBalancer lb = ExtensionLoaderConstants.LOADBALANCE_EXTENSION.findExtension(lbName);
      if (lb == null) {
        throw new RuntimeException("no loadbalancer found,name is " + lbName);
      }
      return lb;
    }

  }

}
