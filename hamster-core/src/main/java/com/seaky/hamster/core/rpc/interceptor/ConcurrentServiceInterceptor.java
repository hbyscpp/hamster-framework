package com.seaky.hamster.core.rpc.interceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.exception.ServiceReachMaxConcurrent;
import com.seaky.hamster.core.rpc.utils.Utils;

@ServiceInterceptorAnnotation(name = "current",
    phases = {ProcessPhase.SERVER_CALL_SERVICE, ProcessPhase.CLIENT_CALL_CLUSTER_SERVICE})
public class ConcurrentServiceInterceptor extends DefaultServiceInterceptor {
  private static ConcurrentHashMap<String, AtomicInteger> serviceCurrentAccessNumer =
      new ConcurrentHashMap<String, AtomicInteger>();

  protected boolean preServerProcess(ServiceContext context) {

    int max = getConcurrentNum(ServiceContextUtils.getProviderConfig(context), true);
    if (max <= 0)
      return true;
    boolean isOk = checkReachMaxConcurrent(
        Utils.generateKey("server", ServiceContextUtils.getServiceName(context),
            ServiceContextUtils.getApp(context), ServiceContextUtils.getVersion(context)),
        max);
    if (isOk)
      return true;
    ServiceContextUtils.getResponse(context)
        .setResult(new ServiceReachMaxConcurrent(ServiceContextUtils.getApp(context),
            ServiceContextUtils.getServiceName(context), ServiceContextUtils.getVersion(context),
            true));

    return false;
  }

  protected boolean preClientProcess(ServiceContext context) {
    int max = getConcurrentNum(ServiceContextUtils.getReferenceConfig(context), false);
    if (max <= 0)
      return true;
    boolean isOk = checkReachMaxConcurrent(Utils.generateKey("client",
        ServiceContextUtils.getServiceName(context), ServiceContextUtils.getReferenceApp(context),
        ServiceContextUtils.getReferenceVersion(context)), max);
    if (isOk)
      return true;
    ServiceContextUtils.getResponse(context)
        .setResult(new ServiceReachMaxConcurrent(ServiceContextUtils.getReferenceApp(context),
            ServiceContextUtils.getServiceName(context),
            ServiceContextUtils.getReferenceVersion(context), false));

    return false;
  }

  protected void serverCompleteProcess(ServiceContext context, Throwable e) {

    decrCurConcurrentNum(Utils.generateKey("server", ServiceContextUtils.getServiceName(context),
        ServiceContextUtils.getApp(context), ServiceContextUtils.getVersion(context)));
  }

  protected void clientCompleteProcess(ServiceContext context, Throwable e) {
    decrCurConcurrentNum(Utils.generateKey("client", ServiceContextUtils.getServiceName(context),
        ServiceContextUtils.getReferenceApp(context),
        ServiceContextUtils.getReferenceVersion(context)));
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
