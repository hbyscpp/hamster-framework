package com.seaky.hamster.core.rpc.client;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.apache.commons.lang.StringUtils;

import com.google.common.util.concurrent.SettableFuture;
import com.gs.collections.impl.list.mutable.FastList;
import com.seaky.hamster.core.rpc.client.cluster.ClusterService;
import com.seaky.hamster.core.rpc.client.cluster.ClusterServiceFactory;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.client.router.ServiceRouter;
import com.seaky.hamster.core.rpc.common.DefaultServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.exception.NoServiceAvailable;
import com.seaky.hamster.core.rpc.exception.ServiceNotFoundException;
import com.seaky.hamster.core.rpc.executor.ServiceThreadpool;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.utils.Utils;

import io.netty.util.concurrent.ImmediateEventExecutor;

public class AsynServiceExecutor<Req, Rsp> {

  private AbstractClient<Req, Rsp> client;

  public AsynServiceExecutor(AbstractClient<Req, Rsp> client) {
    this.client = client;
  }

  public void callService(ReferenceServiceRequest request, SettableFuture<Object> result,
      EndpointConfig config) {



    ServiceContext sc = new DefaultServiceContext(ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
    ServiceContextUtils.setServiceName(sc, request.getServiceName());
    ServiceContextUtils.setReferenceApp(sc, request.getReferenceApp());
    ServiceContextUtils.setReferenceVersion(sc, request.getReferenceVersion());
    ServiceContextUtils.setReferenceGroup(sc, request.getReferenceGroup());
    ServiceContextUtils.setRequestParams(sc, request.getParams());
    ServiceContextUtils.setRequestAttachments(sc, new Attachments());
    ServiceContextUtils.setReferenceConfig(sc, config);
    ServiceContextUtils.setResponse(sc, new Response());
    boolean notUseThreadpoolConfig =
        config.getValueAsBoolean(ConfigConstans.REFERENCE_ASYNPOOL_THREAD_EXE, false);

    if (!notUseThreadpoolConfig) {
      // 异步计算线程之中执行
      CallRemoteServiceTask<Req, Rsp> task = new CallRemoteServiceTask<Req, Rsp>(client, sc, result,
          ClientResourceManager.getAsynExecutorPool());
      ClientResourceManager.getAsynExecutorPool().execute(task);
    } else {
      // 线程池执行
      String serviceThreadpoolName = config.get(ConfigConstans.REFERENCE_THREADPOOL_NAME,
          ConfigConstans.REFERENCE_THREADPOOL_NAME_DEFAULT);
      ServiceThreadpool pool = null;
      if (ConfigConstans.REFERENCE_THREADPOOL_NAME_DEFAULT.equals(serviceThreadpoolName)) {
        pool = ClientResourceManager.getServiceThreadpoolManager().createDefault();
      } else {
        int maxQueue = config.getValueAsInt(ConfigConstans.REFERENCE_THREADPOOL_MAXQUEUE,
            ConfigConstans.REFERENCE_THREADPOOL_MAXQUEUE_DEFAULT);

        int maxThread = config.getValueAsInt(ConfigConstans.REFERENCE_THREADPOOL_MAXSIZE,
            ConfigConstans.REFERENCE_THREADPOOL_MAXSIZE_DEFAULT);

        pool = ClientResourceManager.getServiceThreadpoolManager().create(serviceThreadpoolName,
            maxThread, maxQueue);
      }

      CallRemoteServiceTask<Req, Rsp> task =
          new CallRemoteServiceTask<Req, Rsp>(client, sc, result, pool);
      pool.execute(task);
    }

  }

  // 代表每一个请求的任务
  private static class CallRemoteServiceTask<Req, Rsp> implements Runnable {

    // 客户端最终的结果
    private SettableFuture<Object> finalResult;

    private ServiceContext context;

    private AbstractClient<Req, Rsp> client;

    private Executor executor;

    public CallRemoteServiceTask(AbstractClient<Req, Rsp> client, ServiceContext context,
        SettableFuture<Object> result, Executor executor) {
      this.client = client;
      this.context = context;
      this.finalResult = result;
      this.executor = executor;
    }

    @Override
    public void run() {

      // 1执行interceptor
      final List<ServiceInterceptor> interceptors = client.getServiceInterceptors(
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
          ServiceContextUtils.getReferenceVersion(context),
          ServiceContextUtils.getReferenceGroup(context), ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
      final int size = interceptors == null ? 0 : interceptors.size();
      if (size > 0) {
        // 无需发送网络请求
        boolean success = client.getClientInterceptorService().preProcess(context, interceptors);
        if (!success) {
          // 无需走postProcess流程，直接依据context之中结果设置
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
        if (sds == null || sds.size() == 0)
          throw new NoServiceAvailable(
              context.getAttribute(ServiceContext.SERVICENAME, String.class));
        loadBalancer = getLoadBalancer();
        clusterService = getClusterService(loadBalancer);
        clusterService.process(sds, executor, result);
      } catch (Exception e) {
        // 执行出现问题
        completeProcess(interceptors, size, e);
        return;
      }
      result.addListener(new Runnable() {
        @Override
        public void run() {
          try {
            // 未发生任何异常
            Object obj = result.get();
            ServiceContextUtils.getResponse(context).setResult(obj);
            postProcess(interceptors, size);
            return;
          } catch (InterruptedException e) {
            completeProcess(interceptors, size, e);
            return;
          } catch (ExecutionException e) {
            // 调用出现异常
            Throwable innerException = e.getCause();
            // 远程服务未返回结果,不执行postProcess
            completeProcess(interceptors, size, innerException);
            return;
          }
        }

      }, ImmediateEventExecutor.INSTANCE);

    }

    private void completeProcess(final List<ServiceInterceptor> receiveInterceptors, final int size,
        Throwable e) {
      client.getClientInterceptorService().triggerComplete(context, receiveInterceptors, size, e);
      client.getClientInterceptorService().setFuture(context, finalResult);
      return;
    }

    private void postProcess(final List<ServiceInterceptor> interceptors, final int size) {
      try {
        client.getClientInterceptorService().postProcess(context, interceptors);
        client.getClientInterceptorService().triggerComplete(context, interceptors, size, null);
        client.getClientInterceptorService().setFuture(context, finalResult);
        return;
      } catch (Exception e1) {
        client.getClientInterceptorService().triggerComplete(context, interceptors, size, e1);
        client.getClientInterceptorService().setFuture(context, finalResult);
        return;
      }
    }

    private ClusterService<Req, Rsp> getClusterService(ServiceLoadBalancer loadbalancer) {
      String clusterName = ServiceContextUtils.getReferenceConfig(context)
          .get(ConfigConstans.REFERENCE_CLUSTER, ConfigConstans.REFERENCE_CLUSTER_DEFAULT);
      ClusterServiceFactory csf = AbstractClient.clusterExtension.findExtension(clusterName);
      if (csf == null) {
        throw new RuntimeException("no service cluster found ");
      }
      // 每个请求创建集群实例对象
      return csf.createService(client, loadbalancer, context);
    }

    private List<ServiceProviderDescriptor> chooseServiceInstances() {
      // 2获取服务的所有实例
      Collection<ServiceProviderDescriptor> allServiceDescriptors =
          client.getAllServices(ServiceContextUtils.getServiceName(context));
      if (allServiceDescriptors == null || allServiceDescriptors.size() == 0) {
        throw new ServiceNotFoundException(ServiceContextUtils.getServiceName(context));
      }
      List<ServiceProviderDescriptor> allSd = FastList.newList();
      // 3选择有配置的实例
      for (ServiceProviderDescriptor sd : allServiceDescriptors) {
        // 匹配相同参数
        if (compareParam(ServiceContextUtils.getRequestParams(context), sd)
            && Utils.isVersionComp(ServiceContextUtils.getReferenceVersion(context),
                sd.getVersion())
            && Utils.isGroupMatch(ServiceContextUtils.getReferenceGroup(context), sd.getGroup())
            && client.protocolExtensionFactory.protocolName().equals(sd.getProtocol())) {
          allSd.add(sd);
        }
      }
      if (allSd.size() == 0) {
        throw new ServiceNotFoundException(ServiceContextUtils.getServiceName(context));
      }
      // 4 route 从实例中选择符合要求的实例
      String routerName = ServiceContextUtils.getReferenceConfig(context)
          .get(ConfigConstans.REFERENCE_ROUTER, ConfigConstans.REFERENCE_ROUTER_DEFAULT);
      ServiceRouter router = AbstractClient.routerExtension.findExtension(routerName);
      if (router == null) {
        throw new RuntimeException("no service router found,router name " + routerName);
      }
      // 选出可以提供服务的集群实例
      List<ServiceProviderDescriptor> sds = router.choose(allSd, context);
      return sds;
    }

    private boolean compareParam(Object[] params, ServiceProviderDescriptor sd) {
      // TODO return type 是否要check
      // if (!StringUtils.equals(config.getReturnType().getName(),
      // sd.getReturnType()))
      // return false;
      int leftLength = params == null ? 0 : params.length;
      int rightlength = sd.getParamTypes() == null ? 0 : sd.getParamTypes().length;
      if (leftLength != rightlength)
        return false;
      if (leftLength == 0)
        return true;

      for (int i = 0; i < leftLength; ++i) {
        if (params[i] == null
            || (!StringUtils.equals(params[i].getClass().getName(), sd.getParamTypes()[i]))) {
          // TODO 测试下
          try {
            params[i] = Class.forName(sd.getParamTypes()[i]).cast(params[i]);
          } catch (ClassNotFoundException | ClassCastException e) {
            return false;
          }
        }
      }
      return true;
    }

    private ServiceLoadBalancer getLoadBalancer() {
      String lbName = ServiceContextUtils.getReferenceConfig(context).get(
          ConfigConstans.REFERENCE_LOADBALANCER, ConfigConstans.REFERENCE_LOADBALANCER_DEFAULT);
      ServiceLoadBalancer lb = AbstractClient.loadBalanceExtension.findExtension(lbName);
      if (lb == null) {
        throw new RuntimeException("no loadbalancer found");
      }
      return lb;
    }

  }

}
