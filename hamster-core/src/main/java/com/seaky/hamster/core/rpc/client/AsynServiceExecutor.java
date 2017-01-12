package com.seaky.hamster.core.rpc.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.client.cluster.ClusterService;
import com.seaky.hamster.core.rpc.client.cluster.ClusterServiceFactory;
import com.seaky.hamster.core.rpc.client.loadbalancer.ServiceLoadBalancer;
import com.seaky.hamster.core.rpc.client.router.ServiceRouter;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.common.DefaultServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.config.ReadOnlyEndpointConfig;
import com.seaky.hamster.core.rpc.exception.NoRouteServiceProviderException;
import com.seaky.hamster.core.rpc.exception.NoServiceProviderMatchException;
import com.seaky.hamster.core.rpc.exception.RpcTimeoutException;
import com.seaky.hamster.core.rpc.exception.ServiceProviderNotFoundException;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.trace.ClientCallExceptionTrace;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;
import com.seaky.hamster.core.rpc.utils.Utils;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import net.jodah.failsafe.CircuitBreaker;

public class AsynServiceExecutor<Req, Rsp> {

  private AbstractClient<Req, Rsp> client;

  public AsynServiceExecutor(AbstractClient<Req, Rsp> client) {
    this.client = client;
  }

  public void callService(final ReferenceServiceRequest request,
      final SettableFuture<Object> result, EndpointConfig config, boolean isPreConnect) {

    // 1初始化context
    ServiceContext sc = new DefaultServiceContext(ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);

    ProtocolRequestHeader header = new ProtocolRequestHeader();
    header.setServiceName(request.getServiceName());
    header.setReferenceApp(request.getReferenceApp());
    header.setReferenceGroup(request.getReferenceGroup());
    header.setReferenceVersion(request.getReferenceVersion());
    ProtocolRequestBody body = new ProtocolRequestBody();
    body.setParams(request.getParams());
    ServiceContextUtils.setRequestHeader(sc, header);
    ServiceContextUtils.setRequestBody(sc, body);
    ServiceContextUtils.setReferenceConfig(sc, new ReadOnlyEndpointConfig(config));
    ProtocolResponseHeader rspheader = new ProtocolResponseHeader();
    ServiceContextUtils.setResponseHeader(sc, rspheader);
    ServiceContextUtils.setResponseBody(sc, new ProtocolResponseBody());
    String serName = config.get(ConfigConstans.REFERENCE_SERIALIZATION,
        client.getProtocolExtensionFactory().defaultSerialization());
    Serializer ser = ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(serName);
    if (ser == null)
      throw new RuntimeException("not found serializer " + serName);
    header.getAttachments().putByte(Constants.SERIALIZATION_ID_KEY, ser.id());

    final long seqNum = client.getAndIncrementSeqNum();
    header.getAttachments().putLong(Constants.SEQ_NUM_KEY, seqNum);
    header.getAttachments().putByte(Constants.MSG_TYPE, Constants.MSG_NORMAL_TYPE);
    ServiceContextUtils.setInterceptorExceptionTrace(sc, new ClientCallExceptionTrace(seqNum));

    if (!isPreConnect) {
      int callTimeout = config.getValueAsInt(ConfigConstans.REFERENCE_CALL_TIMEOUT,
          ConfigConstans.REFERENCE_CALL_TIMEOUT_DEFAULT);
      if (callTimeout > 0) {
        ClientResourceManager.getHashedWheelTimer().newTimeout(new TimerTask() {
          @Override
          public void run(Timeout timeout) throws Exception {
            result.setException(new RpcTimeoutException(String.format(
                "call service %s from app: %s,group:%s,version:%s,seq number:%d timeout",
                request.getServiceName(), request.getReferenceApp(), request.getReferenceGroup(),
                request.getReferenceVersion(), seqNum)));
          }
        }, callTimeout, TimeUnit.MILLISECONDS);
        // TODO 统计超时次数
      }
    }
    boolean notUseThreadpoolConfig =
        config.getValueAsBoolean(ConfigConstans.REFERENCE_ASYNPOOL_THREAD_EXE, false);

    if (!notUseThreadpoolConfig) {
      // 异步计算线程之中执行
      EventExecutor executor = ClientResourceManager.getAsynExecutorPool().next();
      CallRemoteServiceTask<Req, Rsp> task =
          new CallRemoteServiceTask<Req, Rsp>(client, sc, result, executor, isPreConnect);
      executor.execute(task);
    } else {
      // 线程池执行
      String serviceThreadpoolName = threadPoolName(sc);
      EventExecutorGroup pool = null;
      int maxThread = config.getValueAsInt(ConfigConstans.REFERENCE_THREADPOOL_MAXSIZE,
          ConfigConstans.REFERENCE_THREADPOOL_MAXSIZE_DEFAULT);
      int maxQueue = config.getValueAsInt(ConfigConstans.REFERENCE_THREADPOOL_MAXQUEUE,
          ConfigConstans.REFERENCE_THREADPOOL_MAXQUEUE_DEFAULT);
      pool = ClientResourceManager.getServiceThreadpoolManager().create(serviceThreadpoolName,
          maxThread, maxQueue);
      EventExecutor executor = pool.next();
      CallRemoteServiceTask<Req, Rsp> task =
          new CallRemoteServiceTask<Req, Rsp>(client, sc, result, executor, isPreConnect);
      executor.execute(task);
    }

  }

  private String threadPoolName(ServiceContext context) {

    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
    String serviceName = header.getServiceName();
    String app = header.getReferenceApp();
    String serviceVersion = header.getReferenceVersion();
    String group = header.getReferenceGroup();
    String key = Utils.generateKey(serviceName, app, serviceVersion, group);
    return key;
  }

  // 代表每一个请求的任务
  private static class CallRemoteServiceTask<Req, Rsp> implements Runnable {

    // 客户端最终的结果
    private SettableFuture<Object> finalResult;

    private ServiceContext context;

    private AbstractClient<Req, Rsp> client;

    private EventExecutor executor;

    private boolean isPreConnect;

    public CallRemoteServiceTask(AbstractClient<Req, Rsp> client, ServiceContext context,
        SettableFuture<Object> result, EventExecutor executor, boolean isPreConnect) {
      this.client = client;
      this.context = context;
      this.finalResult = result;
      this.executor = executor;
      this.isPreConnect = isPreConnect;
    }

    @Override
    public void run() {

      // prProcess 和postProess 保证在同一个线程执行
      // 1执行interceptor
      ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
      final List<ServiceInterceptor> interceptors = client.getServiceInterceptors(
          header.getServiceName(), header.getReferenceApp(), header.getReferenceVersion(),
          header.getReferenceGroup(), ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE);
      final int size = interceptors == null ? 0 : interceptors.size();
      if (size > 0 && !isPreConnect) {
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
        sds = chooseServiceInstances(isPreConnect);
        loadBalancer = getLoadBalancer();
        clusterService = getClusterService(loadBalancer);
      } catch (Exception e) {
        // 执行出现问题
        if (!isPreConnect) {
          client.getClientInterceptorService().addException(context,
              ClientCallExceptionTrace.SELECT_CLUSTER_SERVICE_PREPROCESS, header.getServiceName(),
              e);
          postProcess(interceptors, size, e);
          return;
        }
      }
      try {
        clusterService.process(sds, executor, result);
      } catch (Exception e) {
        client.getClientInterceptorService().addException(context,
            ClientCallExceptionTrace.CALL_CLUSTER_SERVICE, header.getServiceName(), e);
        postProcess(interceptors, size, e);
        return;

      }
      result.addListener(new Runnable() {
        @Override
        public void run() {
          try {
            // 未发生任何异常
            Object obj = result.get();
            ServiceContextUtils.getResponseBody(context).setResult(obj);
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
        ServiceContextUtils.getResponseBody(context).setResult(e);
        ServiceContextUtils.getResponseHeader(context).setException(true);

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
        throw new RuntimeException("no service cluster extension found,name is " + clusterName);
      }
      // 每个请求创建集群实例对象
      return csf.createService(client, loadbalancer, context);
    }

    private List<ServiceProviderDescriptor> chooseServiceInstances(boolean isPreConnect)
        throws ClassNotFoundException {
      // 2获取服务的所有实例
      ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
      Collection<ServiceProviderDescriptor> allServiceDescriptors =
          client.getAllServices(header.getServiceName());
      if (allServiceDescriptors == null || allServiceDescriptors.size() == 0) {
        throw new ServiceProviderNotFoundException(
            String.format("service %s,reference app %s,reference group %s,reference version %s",
                header.getServiceName(), header.getReferenceApp(), header.getReferenceGroup(),
                header.getReferenceVersion()));
      }

      List<ServiceProviderDescriptor> allAvaiableSd = new ArrayList<ServiceProviderDescriptor>();
      for (ServiceProviderDescriptor sd : allServiceDescriptors) {
        CircuitBreaker cb = client.getCircuitBreaker(sd.getName(), sd.getApp(), sd.getGroup(),
            sd.getVersion(), sd.getHost(), String.valueOf(sd.getPort()));
        if (cb == null) {
          cb = new CircuitBreaker();
          int openFailNumber = ServiceContextUtils.getReferenceConfig(context).getValueAsInt(
              ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_FAIL_NUMBER,
              ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_FAIL_NUMBER_DEFAULT);
          int openTotalNumber = ServiceContextUtils.getReferenceConfig(context).getValueAsInt(
              ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_TOTAL_NUMBER,
              ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_TOTAL_NUMBER_DEFAULT);
          cb.withFailureThreshold(openFailNumber, openTotalNumber);

          int closeSuccessNumber = ServiceContextUtils.getReferenceConfig(context).getValueAsInt(
              ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_SUCCESS_NUMBER,
              ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_SUCCESS_NUMBER_DEFAULT);
          int closeTotalNumber = ServiceContextUtils.getReferenceConfig(context).getValueAsInt(
              ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_TOTAL_NUMBER,
              ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_TOTAL_NUMBER_DEFAULT);
          cb.withFailureThreshold(closeSuccessNumber, closeTotalNumber);
          cb.withSuccessThreshold(closeSuccessNumber, closeTotalNumber);
          int halfOpenTime = ServiceContextUtils.getReferenceConfig(context).getValueAsInt(
              ConfigConstans.REFERENCE_CIRCUITBREAKER_HALFOPEN_DELAY,
              ConfigConstans.REFERENCE_CIRCUITBREAKER_HALFOPEN_DELAY_DEFAULT);
          cb.withDelay(halfOpenTime, TimeUnit.MILLISECONDS);
          cb = client.putCircuitBreaker(sd.getName(), sd.getApp(), sd.getGroup(), sd.getVersion(),
              sd.getHost(), String.valueOf(sd.getPort()), cb);
        }
        if (!cb.allowsExecution()) {
          // 排除节点
          continue;
        }
        allAvaiableSd.add(sd);
      }

      if (allAvaiableSd.size() == 0) {
        throw new ServiceProviderNotFoundException(
            String.format("service %s,reference app %s,reference group %s,reference version %s",
                header.getServiceName(), header.getReferenceApp(), header.getReferenceGroup(),
                header.getReferenceVersion()));
      }
      List<ServiceProviderDescriptor> allSd = new ArrayList<ServiceProviderDescriptor>();
      String addesses = ServiceContextUtils.getReferenceConfig(context)
          .get(ConfigConstans.REFERENCE_SERVICE_PROVIDER_ADDRESSES, null);
      Map<String, Integer> directHostAndPorts = addressToMap(addesses);
      // 3选择有配置的实例
      for (ServiceProviderDescriptor sd : allAvaiableSd) {

        // 匹配相同参数和查看是否是直接指定连接的方式
        // 先判断断路器是否开启，开启的不进入这个之中

        if ((isPreConnect || compareParam(context, sd))
            && Utils.isVersionComp(header.getReferenceVersion(), sd.getVersion())
            && Utils.isGroupMatch(header.getReferenceGroup(), sd.getGroup())
            && client.getProtocolExtensionFactory().protocolName().equals(sd.getProtocol())) {
          if (sd.isForceAccess()) {
            // 强制使用该provider
            allSd.clear();
            allSd.add(sd);
            if (isPreConnect) {
              preConnect(sd);
            }
            break;
          }
          // provider隐藏，客户端只能采用直连的方式
          if (directHostAndPorts != null && directHostAndPorts.size() > 0) {
            // 直连的方式
            if (compareHost(sd, directHostAndPorts)) {
              allSd.add(sd);
              if (isPreConnect) {
                preConnect(sd);
              }
            }
          } else {
            // 非直连
            if (!sd.isHidden()) {
              allSd.add(sd);
              if (isPreConnect) {
                preConnect(sd);
              }
            }
          }
        }
      }
      if (allSd.size() == 0) {
        throw new NoServiceProviderMatchException(
            String.format("service %s,reference app %s,reference group %s,reference version %s",
                header.getServiceName(), header.getReferenceApp(), header.getReferenceGroup(),
                header.getReferenceVersion()));
      }
      if (isPreConnect)
        return allSd;
      // 4 route 从实例中选择符合要求的实例
      String routerName = ServiceContextUtils.getReferenceConfig(context)
          .get(ConfigConstans.REFERENCE_ROUTER, ConfigConstans.REFERENCE_ROUTER_DEFAULT);
      ServiceRouter router = ExtensionLoaderConstants.ROUTER_EXTENSION.findExtension(routerName);
      if (router == null) {
        throw new RuntimeException("no service router extension found,router name is" + routerName);
      }
      // 选出可以提供服务的集群实例
      List<ServiceProviderDescriptor> sds = router.choose(allSd, context);
      if (sds == null || sds.size() == 0)
        throw new NoRouteServiceProviderException(
            String.format("service %s,reference app %s,reference group %s,reference version %s",
                header.getServiceName(), header.getReferenceApp(), header.getReferenceGroup(),
                header.getReferenceVersion()));
      return sds;
    }

    private void preConnect(ServiceProviderDescriptor sd) {
      ClientTransport<Req, Rsp> transport = client.getTransport(sd);
      if (!transport.isConnected())
        transport.connect();
      return;
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

    private boolean compareParam(ServiceContext context, ServiceProviderDescriptor sd)
        throws ClassNotFoundException {
      EndpointConfig config = ServiceContextUtils.getReferenceConfig(context);
      String returnType = config.get(ConfigConstans.REFERENCE_RETURN);

      if (!StringUtils.equals(returnType, sd.getReturnType()))
        return false;

      Object[] params = ServiceContextUtils.getRequestBody(context).getParams();
      return Utils.isMatch(sd.getParamTypes(), params);

    }

    private ServiceLoadBalancer getLoadBalancer() {
      String lbName = ServiceContextUtils.getReferenceConfig(context).get(
          ConfigConstans.REFERENCE_LOADBALANCER, ConfigConstans.REFERENCE_LOADBALANCER_DEFAULT);
      ServiceLoadBalancer lb = ExtensionLoaderConstants.LOADBALANCE_EXTENSION.findExtension(lbName);
      if (lb == null) {
        throw new RuntimeException("no loadbalancer extension found,name is " + lbName);
      }
      return lb;
    }

  }

}
