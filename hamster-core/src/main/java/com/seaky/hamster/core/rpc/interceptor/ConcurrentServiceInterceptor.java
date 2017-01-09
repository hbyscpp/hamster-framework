package com.seaky.hamster.core.rpc.interceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.exception.ServiceReachMaxConcurrent;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.utils.Utils;

@ServiceInterceptorAnnotation(name = "concurrence",
    phases = {ProcessPhase.SERVER_CALL_SERVICE, ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE})
public class ConcurrentServiceInterceptor extends DefaultServiceInterceptor {
  private static ConcurrentHashMap<String, AtomicInteger> serviceCurrentAccessNumer =
      new ConcurrentHashMap<String, AtomicInteger>();

  @Override
  protected void preServerProcess(ServiceContext context) {

    int max = getConcurrentNum(ServiceContextUtils.getProviderConfig(context), true);
    if (max <= 0)
      return;
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
    boolean isOk = checkReachMaxConcurrent(Utils.generateKey("server", header.getServiceName(),
        header.getApp(), header.getVersion(), header.getGroup()), max);
    if (isOk)
      return;
    ServiceContextUtils.getResponseBody(context).setResult(new ServiceReachMaxConcurrent(
        header.getApp(), header.getServiceName(), header.getVersion(), true));
    ServiceContextUtils.getResponseHeader(context).setException(true);

  }

  @Override
  protected void preClientClusterProcess(ServiceContext context) {
    int max = getConcurrentNum(ServiceContextUtils.getReferenceConfig(context), false);
    if (max <= 0)
      return;
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);

    boolean isOk = checkReachMaxConcurrent(Utils.generateKey("client", header.getServiceName(),
        header.getReferenceApp(), header.getReferenceVersion()), max);
    if (isOk)
      return;
    ServiceContextUtils.getResponseBody(context).setResult(new ServiceReachMaxConcurrent(
        header.getReferenceApp(), header.getServiceName(), header.getReferenceVersion(), false));
    ServiceContextUtils.getResponseHeader(context).setException(true);
  }

  @Override
  protected void postServerProcess(ServiceContext context) {
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);

    decrCurConcurrentNum(
        Utils.generateKey("server", header.getServiceName(), header.getApp(), header.getVersion()));
  }

  @Override
  protected void postClientClusterProcess(ServiceContext context) {
    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
    decrCurConcurrentNum(Utils.generateKey("client", header.getServiceName(),
        header.getReferenceApp(), header.getReferenceVersion()));
  }

  private boolean checkReachMaxConcurrent(String key, int maxConcurrent) {
    int curNum = addCurConcurrentNum(key);
    if (curNum > maxConcurrent) {
      decrCurConcurrentNum(key);
      return false;
    }
    return true;

  }

  public int addCurConcurrentNum(String key) {
    AtomicInteger currentNum = serviceCurrentAccessNumer.get(key);
    if (currentNum == null) {
      currentNum = new AtomicInteger();
      AtomicInteger oldNum = serviceCurrentAccessNumer.putIfAbsent(key, currentNum);
      if (oldNum != null) {
        currentNum = oldNum;
      }
    }
    return currentNum.incrementAndGet();
  }

  public void decrCurConcurrentNum(String key) {
    AtomicInteger currentNum = serviceCurrentAccessNumer.get(key);
    if (currentNum == null) {
      return;
    }
    currentNum.decrementAndGet();
  }

  private int getConcurrentNum(EndpointConfig serviceConfig, boolean isServer) {

    if (isServer)

      return serviceConfig.getValueAsInt(ConfigConstans.PROVIDER_MAX_CONCURRENT,
          ConfigConstans.PROVIDER_MAX_CONCURRENT_DEFAULT);
    else
      return serviceConfig.getValueAsInt(ConfigConstans.REFERENCE_MAX_CONCURRENT,
          ConfigConstans.REFERENCE_MAX_CONCURRENT_DEFAULT);
  }
}
