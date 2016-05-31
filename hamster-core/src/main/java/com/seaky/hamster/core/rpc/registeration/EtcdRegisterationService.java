package com.seaky.hamster.core.rpc.registeration;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.executor.NamedThreadFactory;
import com.seaky.hamster.core.rpc.utils.Utils;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;

// 定时pull模型,etcd的watch性能不够好，TTL设置20s，每隔15s客户端更新一次
public class EtcdRegisterationService implements RegisterationService {
  private static Logger logger = LoggerFactory.getLogger("hamster_registeration_service_log");
  // 全局的服务实例缓存
  private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>> serviceDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>>();

  private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>> referDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>>();

  // 本地的注册缓存
  private ConcurrentHashMap<String, ServiceProviderDescriptor> localRegistCache =
      new ConcurrentHashMap<String, ServiceProviderDescriptor>();

  private ConcurrentHashMap<String, ServiceReferenceDescriptor> localReferCache =
      new ConcurrentHashMap<String, ServiceReferenceDescriptor>();

  private ConcurrentHashMap<String, Boolean> watchProviderCache =
      new ConcurrentHashMap<String, Boolean>();

  private ConcurrentHashMap<String, Boolean> watchReferecnceCache =
      new ConcurrentHashMap<String, Boolean>();
  protected EtcdClient client;

  private String basePath;

  private static String PROVIDERS = "/providers";

  private static String CONSUMERS = "/consumers";

  private static int DEFAULT_TTL = 20;

  private static int DEFAULT_PULL_TIME = 15;

  private ScheduledExecutorService ses =
      Executors.newScheduledThreadPool(1, new NamedThreadFactory("RegisterationService Schedule"));

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
    ses.scheduleAtFixedRate(new RegistRunnable(this), DEFAULT_PULL_TIME, DEFAULT_PULL_TIME,
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

      // 注册
      if (Thread.interrupted()) {
        logger.info("register pull thread interrup");
        return;

      }
      // 更新配置，并刷新
      for (ServiceProviderDescriptor sd : registService.localRegistCache.values()) {
        registService.updateLocalCache(sd);
        registService.refreshService(sd, true);
      }
      for (ServiceReferenceDescriptor rd : registService.localReferCache.values()) {
        registService.updateLocalCache(rd);
        registService.refreshReference(rd, true);
      }
      // 更新
      for (String name : registService.watchProviderCache.keySet()) {
        try {
          registService.findServices(name, true);
        } catch (Exception e) {
          logger.error("heartbeat regist service error", e);
        }
      }

      for (String name : registService.watchReferecnceCache.keySet()) {
        try {
          registService.findReferences(name, true);
        } catch (Exception e) {
          logger.error("heartbeat regist service error", e);
        }
      }


    }
  }

  @Override
  public void registServiceProvider(ServiceProviderDescriptor sd) {
    localRegistCache.put(serviceKey(sd), sd);
    registService(sd, false);
  }

  private void registService(ServiceProviderDescriptor sd, boolean isAsyn) {

    try {
      EtcdResponsePromise<EtcdKeysResponse> rsp = client.put(genProviderPath(sd), sd.toString())
          .timeout(5, TimeUnit.SECONDS).ttl(DEFAULT_TTL).send();
      if (!isAsyn) {
        rsp.get();
      }
    } catch (Exception e) {
      if (!isAsyn)
        Utils.throwException(e);
      else {
        logger.error("regist service asyn  error ", e);
      }
    }

  }

  private void refreshService(ServiceProviderDescriptor sd, boolean isAsyn) {

    try {
      EtcdResponsePromise<EtcdKeysResponse> rsp =
          client.refresh(genProviderPath(sd), DEFAULT_TTL).timeout(5, TimeUnit.SECONDS).send();
      if (!isAsyn) {
        rsp.get();
      }
    } catch (Exception e) {
      if (!isAsyn)
        Utils.throwException(e);
      else {
        logger.error("refresh service asyn  error ", e);
      }
    }

  }


  private String genProviderPath(ServiceProviderDescriptor sd) {
    return baseServiceProviderPath(sd.getName())
        + Utils.generateKey(sd.getApp(), sd.getVersion(), sd.getGroup(), sd.getHost(),
            String.valueOf(sd.getPort()), sd.getPid(), String.valueOf(sd.getRegistTime()));
  }

  private String genConsumerPath(ServiceReferenceDescriptor rd) {
    return baseServiceConsumerPath(rd.getServiceName())
        + Utils.generateKey(rd.getReferApp(), rd.getReferVersion(), rd.getReferGroup(),
            rd.getProtocol(), rd.getHost(), rd.getPid(), String.valueOf(rd.getRegistTime()));
  }

  private String baseServiceProviderPath(String name) {
    return basePath + "/" + name + PROVIDERS + "/";
  }

  private String baseServiceConsumerPath(String name) {
    return basePath + "/" + name + CONSUMERS + "/";
  }

  @Override
  public Collection<ServiceProviderDescriptor> findServiceProviders(String name) {
    return findServices(name, false);
  }

  private void updateLocalCache(ServiceProviderDescriptor sd) {
    try {
      EtcdResponsePromise<EtcdKeysResponse> rsp =
          client.get(genProviderPath(sd)).timeout(5, TimeUnit.SECONDS).send();
      EtcdNode node = rsp.get().node;

      ServiceProviderDescriptor newsd = ServiceProviderDescriptor.parseStr(node.value);
      localRegistCache.put(serviceKey(newsd), newsd);

    } catch (EtcdException e) {
      if (e.getErrorCode() != 100) {
        logger.error("", e);
      } else {
        // 不存在再注册
        registService(sd, true);
      }
    } catch (Exception e) {
      logger.error("", e);
    }
  }

  private void updateLocalCache(ServiceReferenceDescriptor rd) {
    try {
      EtcdResponsePromise<EtcdKeysResponse> rsp =
          client.get(genConsumerPath(rd)).timeout(5, TimeUnit.SECONDS).send();
      EtcdNode node = rsp.get().node;

      ServiceReferenceDescriptor newsd = ServiceReferenceDescriptor.parseStr(node.value);
      localReferCache.put(referKey(newsd), newsd);

    } catch (EtcdException e) {
      if (e.getErrorCode() != 100) {
        logger.error("", e);
      } else {
        registReference(rd, true);
      }
    } catch (Exception e) {
      logger.error("", e);
    }
  }

  private Collection<ServiceProviderDescriptor> findServices(String name, boolean isForce) {
    Boolean isWatch = null;
    if (isForce == false)
      isWatch = watchProviderCache.putIfAbsent(name, true);

    if (isWatch == null) {

      // 第一次watch
      try {
        EtcdResponsePromise<EtcdKeysResponse> future =
            client.getDir(baseServiceProviderPath(name)).timeout(5, TimeUnit.SECONDS).send();
        EtcdKeysResponse rsp = future.get();
        // 初始化
        initServiceCache(name, rsp.node, true);
        // 注册监听
      } catch (Exception e) {
        if (e instanceof EtcdException) {
          if (((EtcdException) e).errorCode == 100)
            return null;
        }
        logger.error("", e);
      }
    }
    // 继续使用缓存
    ConcurrentHashMap<String, ServiceProviderDescriptor> allSd = serviceDescriptors.get(name);
    if (allSd != null)
      return Collections.unmodifiableCollection(allSd.values());
    return null;
  }

  private void initServiceCache(String name, EtcdNode pnode, boolean isProvider) {
    List<EtcdNode> nodes = pnode.nodes;

    if (nodes == null || nodes.size() == 0) {
      // 不存在
      if (isProvider)
        serviceDescriptors.remove(name);
      else
        referDescriptors.remove(name);
      return;
    }
    // 服务提供者
    if (isProvider)
      updateProvider(name, nodes);
    else
      updateReference(name, nodes);
  }

  private void updateProvider(String name, List<EtcdNode> providerNodes) {
    ConcurrentHashMap<String, ServiceProviderDescriptor> allds = new ConcurrentHashMap<>();
    for (EtcdNode n : providerNodes) {
      try {
        ServiceProviderDescriptor sd = ServiceProviderDescriptor.parseStr(n.value);
        String subkey = serviceKey(sd);
        allds.put(subkey, sd);

        // 更新本地缓存
        if (localRegistCache.containsKey(subkey)) {
          localRegistCache.put(subkey, sd);
        }

      } catch (Exception e) {
        logger.error("init provdier cache {}", n.value, e);
      }
    }
    serviceDescriptors.put(name, allds);
  }

  private void updateReference(String name, List<EtcdNode> providerNodes) {
    ConcurrentHashMap<String, ServiceReferenceDescriptor> allds = new ConcurrentHashMap<>();
    for (EtcdNode n : providerNodes) {
      try {
        ServiceReferenceDescriptor sd = ServiceReferenceDescriptor.parseStr(n.value);

        String subkey = referKey(sd);

        allds.put(subkey, sd);

        // 更新本地缓存
        if (localReferCache.containsKey(subkey)) {
          localReferCache.put(subkey, sd);
        }

      } catch (Exception e) {
        logger.error("init provdier cache {}", n.value, e);
      }
    }
    referDescriptors.put(name, allds);
  }

  private String serviceKey(ServiceProviderDescriptor sd) {
    return Utils.generateKey(sd.getName(), sd.getApp(), sd.getVersion(), sd.getGroup(),
        sd.getHost(), String.valueOf(sd.getPort()));
  }

  @Override
  public ServiceProviderDescriptor findServiceProvider(String app, String name, String version,
      String group, String protocol, String host, int port, String pid, long registTime) {

    String key = Utils.generateKey(name, app, version, group, host, String.valueOf(port));
    ServiceProviderDescriptor sd = localRegistCache.get(key);
    if (sd != null)
      return sd;
    findServiceProviders(name);
    ConcurrentHashMap<String, ServiceProviderDescriptor> sds = serviceDescriptors.get(name);
    if (sds == null)
      return null;
    return sds.get(key);
  }

  @Override
  public void unregistServiceProvider(ServiceProviderDescriptor sd) {
    localRegistCache.remove(serviceKey(sd));
    try {
      client.delete(genProviderPath(sd)).timeout(5, TimeUnit.SECONDS).send();
    } catch (IOException e) {
      logger.error("delete service  error {}", sd.toString(), e);
    }

  }

  @Override
  public void registServiceReference(ServiceReferenceDescriptor rd) {
    localReferCache.put(referKey(rd), rd);
    registReference(rd, false);
  }


  public void registReference(ServiceReferenceDescriptor rd, boolean isAsyn) {
    try {
      EtcdResponsePromise<EtcdKeysResponse> rsp = client.put(genConsumerPath(rd), rd.toString())
          .timeout(5, TimeUnit.SECONDS).ttl(DEFAULT_TTL).send();
      if (!isAsyn)
        rsp.get();
    } catch (IOException | EtcdException | EtcdAuthenticationException | TimeoutException e) {
      if (!isAsyn)
        Utils.throwException(e);
      else
        logger.error("regist reference ", e);
    }
  }

  private void refreshReference(ServiceReferenceDescriptor rd, boolean isAsyn) {
    try {
      EtcdResponsePromise<EtcdKeysResponse> rsp =
          client.refresh(genConsumerPath(rd), DEFAULT_TTL).timeout(5, TimeUnit.SECONDS).send();
      if (!isAsyn)
        rsp.get();
    } catch (IOException | EtcdException | EtcdAuthenticationException | TimeoutException e) {
      if (!isAsyn)
        Utils.throwException(e);
      else
        logger.error("regist reference ", e);
    }
  }

  private String referKey(ServiceReferenceDescriptor rd) {
    return Utils.generateKey(rd.getServiceName(), rd.getReferApp(), rd.getReferGroup(),
        rd.getReferVersion(), rd.getProtocol(), rd.getHost(), rd.getPid(),
        String.valueOf(rd.getRegistTime()));
  }

  @Override
  public void unregistServiceReference(ServiceReferenceDescriptor rd) {
    localReferCache.remove(referKey(rd));
    try {
      client.delete(genConsumerPath(rd)).timeout(5, TimeUnit.SECONDS).send();
    } catch (IOException e) {
      logger.error("delete service reference error {}", rd.toString(), e);
    }
  }



  @Override
  public synchronized void close() {

    try {
      for (ServiceProviderDescriptor sd : localRegistCache.values()) {
        unregistServiceProvider(sd);
      }
      for (ServiceReferenceDescriptor rd : localReferCache.values()) {
        unregistServiceReference(rd);
      }
      shutdownTimerTask();
      client.close();
    } catch (IOException e) {
      logger.error("close etcd registation service error ", e);
    } finally {
      serviceDescriptors.clear();
      referDescriptors.clear();
      localRegistCache.clear();
      localReferCache.clear();
    }

  }

  private void shutdownTimerTask() {
    ses.shutdownNow();
  }

  @Override
  public Collection<ServiceReferenceDescriptor> findServiceReferences(String name) {
    return findReferences(name, false);
  }

  private Collection<ServiceReferenceDescriptor> findReferences(String name, boolean isForce) {
    Boolean isWatch = null;
    if (isForce == false)
      isWatch = watchReferecnceCache.putIfAbsent(name, true);

    if (isWatch == null) {

      // 第一次watch
      try {
        EtcdResponsePromise<EtcdKeysResponse> future =
            client.getDir(baseServiceProviderPath(name)).timeout(5, TimeUnit.SECONDS).send();
        EtcdKeysResponse rsp = future.get();
        // 初始化
        initServiceCache(name, rsp.node, false);
        // 注册监听
      } catch (Exception e) {
        if (e instanceof EtcdException) {
          if (((EtcdException) e).errorCode == 100)
            return null;
        }
        logger.error("", e);
      }
    }
    // 继续使用缓存
    ConcurrentHashMap<String, ServiceReferenceDescriptor> allSd = referDescriptors.get(name);
    if (allSd != null)
      return Collections.unmodifiableCollection(allSd.values());
    return null;
  }

  @Override
  public ServiceReferenceDescriptor findServiceReference(String referApp, String name,
      String version, String group, String protocol, String host, String pid, long registTime) {
    String key = Utils.generateKey(name, referApp, group, version, protocol, host, pid,
        String.valueOf(registTime));
    ServiceReferenceDescriptor rd = localReferCache.get(key);
    if (rd != null)
      return rd;
    findServiceReferences(name);
    ConcurrentHashMap<String, ServiceReferenceDescriptor> serviceReferences =
        referDescriptors.get(name);
    if (serviceReferences == null)
      return null;
    return serviceReferences.get(key);
  }



}
