package com.seaky.hamster.core.rpc.registeration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.executor.NamedThreadFactory;
import com.seaky.hamster.core.rpc.utils.Utils;

import io.netty.util.HashedWheelTimer;
import mousio.client.promises.ResponsePromise;
import mousio.client.promises.ResponsePromise.IsSimplePromiseResponseHandler;
import mousio.client.retry.RetryPolicy;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;

public class EtcdRegisterationService implements RegisterationService {
  private static Logger logger = LoggerFactory.getLogger(EtcdRegisterationService.class);

  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        closeTimer();
        super.run();
      }
    });
  }
  // 全局的服务实例缓存
  private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>> serviceDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>>();

  private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>> referDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>>();

  // 本地的注册缓存
  private ConcurrentHashMap<String, ServiceProviderDescriptor> localRegistCache =
      new ConcurrentHashMap<String, ServiceProviderDescriptor>();

  private ConcurrentHashMap<String, ServiceReferenceDescriptor> localReferCache =
      new ConcurrentHashMap<String, ServiceReferenceDescriptor>();

  private ConcurrentHashMap<String, EtcdResponsePromise<EtcdKeysResponse>> watchCache =
      new ConcurrentHashMap<String, EtcdResponsePromise<EtcdKeysResponse>>();

  private ConcurrentHashMap<String, Boolean> watchCacheBoolean =
      new ConcurrentHashMap<String, Boolean>();
  protected EtcdClient client;

  private String basePath;

  private static String PROVIDERS = "/providers";

  private static String CONSUMERS = "/consumers";

  private static int DEFAULT_TTL = 15;

  private ScheduledExecutorService ses = Executors
      .newSingleThreadScheduledExecutor(new NamedThreadFactory("RegisterationService Schedule"));

  private volatile boolean isClose = false;

  public EtcdRegisterationService(String basePath, String urls) {

    if (Utils.isContainPathSeparator(basePath))
      throw new RuntimeException("basePath can not contain / or \\");

    String[] urlarray = null;
    if (urls != null)
      urlarray = urls.split(Constants.COMMA);
    if (urlarray != null && urlarray.length != 0) {
      URI[] uris = new URI[urlarray.length];
      int i = 0;
      for (String url : urlarray) {
        uris[i] = URI.create(url);
        ++i;
      }
      client = new EtcdClient(uris);
    } else {
      client = new EtcdClient();
    }
    this.basePath = basePath;
    ses.scheduleAtFixedRate(new RegistRunnable(this), (DEFAULT_TTL * 2) / 3, (DEFAULT_TTL * 2) / 3,
        TimeUnit.SECONDS);
  }

  // 定时注册的线程
  private static class RegistRunnable implements Runnable {

    private EtcdRegisterationService registService;

    public RegistRunnable(EtcdRegisterationService registService) {
      this.registService = registService;
    }

    @Override
    public void run() {
      if (Thread.interrupted())
        return;
      for (ServiceProviderDescriptor sd : registService.localRegistCache.values()) {
        try {
          if (Thread.interrupted())
            return;
          registService.registService(sd, true);
        } catch (Exception e) {
          logger.error("heartbeat regist service error", e);
        }
      }
      for (ServiceReferenceDescriptor sd : registService.localReferCache.values()) {
        try {
          if (Thread.interrupted())
            return;
          registService.registReference(sd, true);
        } catch (Exception e) {
          logger.error("heartbeat regist refer error", e);
        }
      }
    }
  }

  @Override
  public void registService(ServiceProviderDescriptor sd) {
    localRegistCache.put(serviceKey(sd), sd);
    registService(sd, false);
  }

  public void registService(ServiceProviderDescriptor sd, boolean isAsyn) {

    try {
      EtcdResponsePromise<EtcdKeysResponse> rsp =
          client.put(genProviderPath(sd), sd.toString()).ttl(DEFAULT_TTL).send();
      if (!isAsyn) {
        rsp.get();
      }
    } catch (Exception e) {
      Utils.throwException(e);
    }

  }

  private String serviceKey(ServiceProviderDescriptor sd) {
    return Utils.generateKey(sd.getName(), sd.getApp(), sd.getVersion(), sd.getGroup(),
        sd.getHost(), String.valueOf(sd.getPort()));
  }

  private String genProviderPath(ServiceProviderDescriptor sd) {
    return baseServiceProviderPath(sd.getName())
        + Utils.generateKey(sd.getApp(), sd.getVersion(), sd.getGroup(), sd.getHost(),
            String.valueOf(sd.getPort()), sd.getPid(), String.valueOf(sd.getRegistTime()));
  }

  private String genConsumerPath(ServiceReferenceDescriptor rd) {
    return baseServiceConsumerPath(rd.getServiceName())
        + Utils.generateKey(rd.getReferApp(), rd.getReferVersion(), rd.getReferGroup(),
            rd.getProtocol(), rd.getPid(), String.valueOf(rd.getRegistTime()));
  }

  private void registServiceChangeListenser(final String serviceName, long raft) {

    if (isClose)
      return;
    try {

      EtcdResponsePromise<EtcdKeysResponse> rspFutrue =
          client.getDir(baseServicePath(serviceName)).waitForChange(raft).recursive().send();

      rspFutrue.addListener(new IsSimplePromiseResponseHandler<EtcdKeysResponse>() {

        @Override
        public void onResponse(ResponsePromise<EtcdKeysResponse> response) {
          try {
            EtcdKeysResponse rsp = response.get();
            // 处理
            processChange(serviceName, rsp);
            watchCache.remove(serviceName);
            if (!isClose)
              registServiceChangeListenser(serviceName, rsp.node.modifiedIndex + 1);
          } catch (Exception e) {
            // 重新forceload，并监听
            if (!isClose)
              findServices(serviceName, true);
          }
        }
      });

      watchCache.put(serviceName, rspFutrue);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void processChange(String name, EtcdKeysResponse rsp) {
    // 目录不处理
    if (rsp.node.dir)
      return;

    String providerPath = baseServiceProviderPath(name);

    String consumerPath = baseServiceConsumerPath(name);

    if (rsp.node.key.startsWith(providerPath)) {
      // 提供者

      // 更新操作
      if ("set".equals(rsp.action) || "update".equals(rsp.action)) {

        if (!StringUtils.equals(rsp.node.value, rsp.prevNode.value)) {
          updateService(name, rsp.node.value);
        }
      } else if ("delete".equals(rsp.action) || "expire".equals(rsp.action)) {
        deleteService(name, rsp.prevNode.value);
      }

    } else if (rsp.node.key.startsWith(consumerPath)) {
      if ("set".equals(rsp.action) || "update".equals(rsp.action)) {
        if (!StringUtils.equals(rsp.node.value, rsp.prevNode.value)) {
          updateConsumer(name, rsp.node.value);
        }
      } else if ("delete".equals(rsp.action) || "expire".equals(rsp.action)) {
        deleteConsumer(name, rsp.prevNode.value);
      }
    }
  }

  private String baseServicePath(String name) {
    return basePath + "/" + name;
  }

  private String baseServiceProviderPath(String name) {
    return basePath + "/" + name + PROVIDERS + "/";
  }

  private String baseServiceConsumerPath(String name) {
    return basePath + "/" + name + CONSUMERS + "/";
  }

  @Override
  public Collection<ServiceProviderDescriptor> findServices(String name) {

    return findServices(name, false);
  }

  private Collection<ServiceProviderDescriptor> findServices(String name, boolean isForce) {

    Boolean isWatch = watchCacheBoolean.putIfAbsent(name, true);

    if (isWatch == null || isForce == true) {

      // 第一次watch
      try {
        EtcdResponsePromise<EtcdKeysResponse> future =
            client.getDir(baseServicePath(name)).recursive().timeout(5, TimeUnit.SECONDS).send();
        EtcdKeysResponse rsp = future.get();
        // 初始化
        initServiceCache(name, rsp.node);
        // 注册监听
        registServiceChangeListenser(name, rsp.etcdIndex + 1);
      } catch (Exception e) {
        watchCacheBoolean.remove(name);
        if (e instanceof EtcdException) {
          if (((EtcdException) e).errorCode == 100)
            return null;
        }
        // 继续使用缓存
        logger.error("", e);
      }
    }

    ConcurrentHashMap<String, ServiceProviderDescriptor> allSd = serviceDescriptors.get(name);
    if (allSd != null)
      return Collections.unmodifiableCollection(allSd.values());
    return null;
  }

  private void initServiceCache(String name, EtcdNode pnode) {
    List<EtcdNode> subNodes = pnode.nodes;

    if (subNodes == null || subNodes.size() == 0) {
      // 不存在
      serviceDescriptors.remove(name);
      referDescriptors.remove(name);
      return;
    }

    for (EtcdNode node : subNodes) {

      if (node.key.endsWith(PROVIDERS)) {

        // 服务提供者
        List<EtcdNode> providerNodes = node.nodes;
        genInitServiceCache(name, providerNodes);

      } else if (node.key.endsWith(CONSUMERS)) {
        // 服务消费者
        List<EtcdNode> consumerNodes = node.nodes;
        genInitConsumerCache(name, consumerNodes);
      }
    }
  }

  private void genInitServiceCache(String name, List<EtcdNode> nodes) {

    if (nodes != null) {
      for (EtcdNode n : nodes) {
        updateService(name, n.value);
      }
    }
  }

  private void genInitConsumerCache(String name, List<EtcdNode> nodes) {
    if (nodes != null) {
      for (EtcdNode n : nodes) {
        updateConsumer(name, n.value);
      }
    }
  }

  private void updateService(String name, String value) {

    ServiceProviderDescriptor sd = ServiceProviderDescriptor.parseStr(value);

    ConcurrentHashMap<String, ServiceProviderDescriptor> allds = serviceDescriptors.get(name);

    if (allds == null) {
      allds = new ConcurrentHashMap<String, ServiceProviderDescriptor>();
      ConcurrentHashMap<String, ServiceProviderDescriptor> oldds =
          serviceDescriptors.putIfAbsent(name, allds);
      if (oldds != null) {
        allds = oldds;
      }
    }

    String subkey = serviceKey(sd);

    allds.put(subkey, sd);

    // 更新本地缓存
    if (localRegistCache.containsKey(subkey)) {
      localRegistCache.put(subkey, sd);
    }

  }

  private void deleteConsumer(String name, String value) {

    ServiceReferenceDescriptor rd = ServiceReferenceDescriptor.parseStr(value);

    ConcurrentHashMap<String, ServiceReferenceDescriptor> allds = referDescriptors.get(name);

    if (allds == null) {
      return;
    }
    if (rd.getAddressPairs().size() != 0) {

      for (String pair : rd.getAddressPairs()) {
        String key = Utils.generateKey(rd.getServiceName(), rd.getReferApp(), rd.getReferGroup(),
            rd.getReferVersion(), rd.getProtocol(), pair);
        allds.remove(key, rd);
      }
    }
  }

  private void deleteService(String name, String value) {

    ServiceProviderDescriptor sd = ServiceProviderDescriptor.parseStr(value);

    ConcurrentHashMap<String, ServiceProviderDescriptor> allds = serviceDescriptors.get(name);

    if (allds == null) {
      return;
    }
    String subkey = serviceKey(sd);
    allds.remove(subkey);
  }

  private void updateConsumer(String name, String value) {
    ServiceReferenceDescriptor rd = ServiceReferenceDescriptor.parseStr(value);
    ConcurrentHashMap<String, ServiceReferenceDescriptor> allds = referDescriptors.get(name);
    if (allds == null) {
      allds = new ConcurrentHashMap<String, ServiceReferenceDescriptor>();
      ConcurrentHashMap<String, ServiceReferenceDescriptor> oldds =
          referDescriptors.putIfAbsent(name, allds);
      if (oldds != null) {
        allds = oldds;
      }
    }
    if (rd.getAddressPairs().size() != 0) {

      for (String pair : rd.getAddressPairs()) {
        String key = Utils.generateKey(rd.getServiceName(), rd.getReferApp(), rd.getReferGroup(),
            rd.getReferVersion(), rd.getProtocol(), pair);
        allds.put(key, rd);
      }
    }
    String subkey = referKey(rd);

    // 更新本地缓存
    if (localReferCache.containsKey(subkey)) {
      localReferCache.put(subkey, rd);
    }

  }

  @Override
  public ServiceProviderDescriptor findService(String app, String name, String version,
      String group, String protocol, String host, int port) {

    String key = Utils.generateKey(name, app, version, group, host, String.valueOf(port));
    ServiceProviderDescriptor sd = localRegistCache.get(key);
    if (sd != null)
      return sd;
    ConcurrentHashMap<String, ServiceProviderDescriptor> sds = serviceDescriptors.get(name);
    if (sds == null)
      return null;
    return sds.get(key);
  }

  @Override
  public void unregistService(ServiceProviderDescriptor sd) {
    localRegistCache.remove(serviceKey(sd));
    try {
      client.delete(genProviderPath(sd)).send();
    } catch (IOException e) {
      logger.error("delete service  error {}", sd.toString(), e);
    }

  }

  @Override
  public void registReference(ServiceReferenceDescriptor rd) {
    localReferCache.put(referKey(rd), rd);
    registReference(rd, false);
  }

  private static void closeTimer() {

    try {
      Thread.sleep(1000);
      Field f = RetryPolicy.class.getDeclaredField("timer");
      f.setAccessible(true);
      HashedWheelTimer timer = (HashedWheelTimer) f.get(null);
      timer.stop();
    } catch (Exception e) {
      logger.error("close timer error ", e);
    }

  }

  public void registReference(ServiceReferenceDescriptor rd, boolean isAsyn) {
    try {

      EtcdResponsePromise<EtcdKeysResponse> rsp =
          client.put(genConsumerPath(rd), rd.toString()).ttl(DEFAULT_TTL).send();
      if (!isAsyn)
        rsp.get();
    } catch (IOException | EtcdException | EtcdAuthenticationException | TimeoutException e) {
      Utils.throwException(e);
    }
  }

  private String referKey(ServiceReferenceDescriptor rd) {
    return Utils.generateKey(rd.getServiceName(), rd.getReferApp(), rd.getReferGroup(),
        rd.getReferVersion(), rd.getProtocol(), rd.getPid(), String.valueOf(rd.getRegistTime()));
  }

  @Override
  public void unregistReference(ServiceReferenceDescriptor rd) {
    localReferCache.remove(referKey(rd));
    try {
      client.delete(genConsumerPath(rd)).send();
    } catch (IOException e) {
      logger.error("delete service reference error {}", rd.toString(), e);
    }
  }

  @Override
  public ServiceReferenceDescriptor findReferenceDescriptor(String referApp, String name,
      String version, String group, String protocol, String referHost, int referPort, String host,
      int port) {
    ConcurrentHashMap<String, ServiceReferenceDescriptor> sds = referDescriptors.get(name);
    if (sds == null)
      return null;
    String pair = Utils.createServerAndClientAddress(referHost, referPort, host, port);
    String key = Utils.generateKey(name, referApp, group, version, protocol, pair);
    return sds.get(key);
  }

  @Override
  public synchronized void close() {

    try {

      for (ServiceProviderDescriptor sd : localRegistCache.values()) {
        unregistService(sd);
      }
      for (ServiceReferenceDescriptor rd : localReferCache.values()) {
        unregistReference(rd);
      }
      shutdownTimerTask();
      client.close();
      closeTimer();
    } catch (IOException e) {
      logger.error("close etcd registation service error ", e);
    } finally {
      serviceDescriptors.clear();
      referDescriptors.clear();
      localRegistCache.clear();
      localReferCache.clear();
      watchCache.clear();
    }

  }

  private void shutdownTimerTask() {
    ses.shutdownNow();
  }

  @Override
  public ServiceReferenceDescriptor findReferenceDescriptor(String referApp, String name,
      String version, String group, String protocol, String pid, long registTime) {
    String key = Utils.generateKey(name, referApp, group, version, protocol, pid,
        String.valueOf(registTime));
    return null;
  }

}
