package com.seaky.hamster.admin;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.executor.NamedThreadFactory;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;

public class EtcdRegisterationManageService {

  protected EtcdClient client;

  private String basePath;

  private static String PROVIDERS = "/providers";

  private static String CONSUMERS = "/consumers";

  private static int DEFAULT_TTL = 20;

  private static int DEFAULT_PULL_TIME = 15;

  private static Logger logger = LoggerFactory.getLogger("hamster_registeration_service_log");

  private ScheduledExecutorService ses =
      Executors.newScheduledThreadPool(1, new NamedThreadFactory("RegisterationService Schedule"));

  // 全局的服务实例缓存
  private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>> serviceDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>>();

  private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>> referDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>>();

  // 服务
  private TinkerGraph serviceGraph = new TinkerGraph();

  // 节点视图
  private TinkerGraph nodeGraph = new TinkerGraph();

  private TinkerGraph graph = new TinkerGraph();

  private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>> backserviceDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>>();

  private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>> backreferDescriptors =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>>();


  private TinkerGraph backServiceGraph = new TinkerGraph();

  private TinkerGraph backNodeGraph = new TinkerGraph();

  private TinkerGraph backgraph = new TinkerGraph();



  public EtcdRegisterationManageService(String basePath, String urls) {

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
    ses.scheduleWithFixedDelay(new RegistRunnable(this), 0, DEFAULT_PULL_TIME, TimeUnit.SECONDS);
  }

  private static class RegistRunnable implements Runnable {

    private EtcdRegisterationManageService etcdRegisterationManageService;

    public RegistRunnable(EtcdRegisterationManageService registService) {
      this.etcdRegisterationManageService = registService;
    }

    @Override
    public void run() {

      // 注册
      if (Thread.interrupted()) {
        return;

      }

      // 初始化
      try {
        etcdRegisterationManageService.backreferDescriptors.clear();
        etcdRegisterationManageService.backserviceDescriptors.clear();
        etcdRegisterationManageService.backgraph.clear();
        etcdRegisterationManageService.backNodeGraph.clear();
        etcdRegisterationManageService.backServiceGraph.clear();
        etcdRegisterationManageService.initData();

        etcdRegisterationManageService.referDescriptors =
            etcdRegisterationManageService.backreferDescriptors;
        etcdRegisterationManageService.backreferDescriptors =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceReferenceDescriptor>>();

        etcdRegisterationManageService.serviceDescriptors =
            etcdRegisterationManageService.backserviceDescriptors;
        etcdRegisterationManageService.backserviceDescriptors =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProviderDescriptor>>();

        etcdRegisterationManageService.graph = etcdRegisterationManageService.backgraph;
        etcdRegisterationManageService.backgraph = new TinkerGraph();

        etcdRegisterationManageService.nodeGraph = etcdRegisterationManageService.backNodeGraph;
        etcdRegisterationManageService.backNodeGraph = new TinkerGraph();

        etcdRegisterationManageService.serviceGraph =
            etcdRegisterationManageService.backServiceGraph;
        etcdRegisterationManageService.backServiceGraph = new TinkerGraph();

      } catch (Exception e) {
        logger.error("", e);
      }
    }

  }

  private void initData()
      throws IOException, EtcdException, EtcdAuthenticationException, TimeoutException {
    EtcdResponsePromise<EtcdKeysResponse> future =
        client.getDir(basePath).timeout(5, TimeUnit.SECONDS).send();
    EtcdKeysResponse rsp = future.get();
    List<EtcdNode> serviceNameNodes = rsp.node.getNodes();
    // 遍历所有服务
    for (EtcdNode node : serviceNameNodes) {

      String servciceName = node.key.substring(node.key.lastIndexOf("/") + 1);
      initServiceProvider(servciceName);
    }

    for (EtcdNode node : serviceNameNodes) {

      String servciceName = node.key.substring(node.key.lastIndexOf("/") + 1);
      initServiceReference(servciceName);

    }


  }

  private void initServiceReference(String name) {

    // 第一次watch
    try {
      EtcdResponsePromise<EtcdKeysResponse> future =
          client.getDir(baseServiceConsumerPath(name)).timeout(5, TimeUnit.SECONDS).send();
      EtcdKeysResponse rsp = future.get();
      // 初始化
      initServiceCache(name, rsp.node, false);
      // 注册监听
    } catch (Exception e) {
      if (e instanceof EtcdException) {
        if (((EtcdException) e).errorCode == 100)
          return;
      }
      logger.error("", e);
    }
    // 继续使用缓存
  }

  private String baseServiceProviderPath(String name) {
    return basePath + "/" + name + PROVIDERS + "/";
  }

  private String baseServiceConsumerPath(String name) {
    return basePath + "/" + name + CONSUMERS + "/";
  }

  private void initServiceProvider(String name) {
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
          return;
      }
    }

    // 继续使用缓存
  }

  private void initServiceCache(String name, EtcdNode pnode, boolean isProvider) {
    List<EtcdNode> nodes = pnode.nodes;
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
        Vertex vertex = backgraph.getVertex(sd.getApp());
        if (vertex == null) {
          vertex = backgraph.addVertex(sd.getApp());
        }
        Set<String> names = vertex.getProperty("serviceName");
        if (names == null) {
          names = new HashSet<>();
          vertex.setProperty("serviceName", names);
        }
        names.add(name);

        // 服务视角
        Vertex vertexService = backServiceGraph.getVertex(name);
        if (vertexService == null) {
          vertexService = backServiceGraph.addVertex(name);
        }
        Set<String> apps = vertexService.getProperty("apps");
        if (apps == null) {
          apps = new HashSet<>();
          vertexService.setProperty("apps", apps);
        }
        apps.add(sd.getApp());

        // 机器视角
        Vertex nodeVertexService = backNodeGraph.getVertex(sd.getHost());
        if (nodeVertexService == null) {
          nodeVertexService = backNodeGraph.addVertex(sd.getHost());
        }
        Set<String> nodeApps = nodeVertexService.getProperty("apps");
        if (nodeApps == null) {
          nodeApps = new HashSet<>();
          nodeVertexService.setProperty("apps", nodeApps);
        }
        nodeApps.add(sd.getApp());
      } catch (Exception e) {
        logger.error("init provdier cache {}", n.value, e);
      }
    }
    if (allds.size() > 0)
      backserviceDescriptors.put(name, allds);
  }

  private String serviceKey(ServiceProviderDescriptor sd) {
    return Utils.generateKey(sd.getName(), sd.getApp(), sd.getVersion(), sd.getGroup(),
        sd.getHost(), String.valueOf(sd.getPort()));
  }

  private String referKey(ServiceReferenceDescriptor rd) {
    return Utils.generateKey(rd.getServiceName(), rd.getReferApp(), rd.getReferGroup(),
        rd.getReferVersion(), rd.getProtocol(), rd.getHost(), rd.getPid(),
        String.valueOf(rd.getRegistTime()));
  }

  private void updateReference(String name, List<EtcdNode> providerNodes) {
    ConcurrentHashMap<String, ServiceReferenceDescriptor> allds = new ConcurrentHashMap<>();
    for (EtcdNode n : providerNodes) {
      try {
        ServiceReferenceDescriptor sd = ServiceReferenceDescriptor.parseStr(n.value);
        String subkey = referKey(sd);
        allds.put(subkey, sd);
        Vertex vertex = backgraph.getVertex(sd.getReferApp());
        if (vertex == null) {
          vertex = backgraph.addVertex(sd.getReferApp());
        }
        Set<String> names = vertex.getProperty("referName");
        if (names == null) {
          names = new HashSet<>();
          vertex.setProperty("referName", names);
        }
        names.add(name);

        // 添加依赖关系

        Vertex serviceVertex = backServiceGraph.getVertex(name);
        if (serviceVertex != null) {

          Set<String> apps = (Set<String>) serviceVertex.getProperty("apps");

          if (apps != null) {
            for (String app : apps) {
              Vertex inVertex = backServiceGraph.getVertex(app);
              if (inVertex != null) {
                Edge edge = backgraph.getEdge(sd.getReferApp() + ":" + app);
                if (edge == null) {
                  // 添加直接依赖边
                  edge =
                      backgraph.addEdge(sd.getReferApp() + ":" + app, vertex, inVertex, "dependOn");
                }
                // TODO 具体依赖的服务明细
                Set<String> serviceNames = vertex.getProperty("serviceNames");
                if (serviceNames == null) {
                  serviceNames = new HashSet<>();
                  edge.setProperty("serviceNames", serviceNames);
                }
                serviceNames.add(name);
              }

            }


          }

        }

      } catch (Exception e) {
        logger.error("init provdier cache {}", n.value, e);
      }
    }
    if (allds.size() > 0)
      backreferDescriptors.put(name, allds);
  }

  public List<String> getAllServiceNames() {
    return new ArrayList<String>(serviceDescriptors.keySet());
  }

  public List<String> searchService(String query) {

    if (StringUtils.isBlank(query))
      return getAllServiceNames();
    List<String> result = new ArrayList<>();
    for (String service : serviceDescriptors.keySet()) {
      if (service.indexOf(query) != -1) {
        result.add(service);
      }
    }
    return result;

  }

  public List<String> getAllApp() {

    List<String> appList = new ArrayList<>();
    Iterable<Vertex> vertexs = graph.getVertices();

    for (Vertex v : vertexs) {
      appList.add((String) v.getId());
    }
    return appList;
  }

  public List<String> searchApp(String query) {
    if (StringUtils.isBlank(query))
      return getAllApp();
    List<String> appList = new ArrayList<>();
    Iterable<Vertex> vertexs = graph.getVertices();

    for (Vertex v : vertexs) {
      String id = (String) v.getId();
      if (id.indexOf(query) != -1)
        appList.add(id);
    }
    return appList;
  }

  public List<String> getAllNode() {

    List<String> appList = new ArrayList<>();
    Iterable<Vertex> vertexs = nodeGraph.getVertices();

    for (Vertex v : vertexs) {
      appList.add((String) v.getId());
    }
    return appList;
  }

  public List<String> searchNode(String query) {
    if (StringUtils.isBlank(query))
      return getAllApp();
    List<String> appList = new ArrayList<>();
    Iterable<Vertex> vertexs = nodeGraph.getVertices();

    for (Vertex v : vertexs) {
      String id = (String) v.getId();
      if (id.indexOf(query) != -1)
        appList.add(id);
    }
    return appList;
  }

  public Map<String, List<ServiceInstanceView>> getAllServiceInstance(String serviceName) {

    return getAllServiceInstanceFilterByApp(serviceName, null, null);
  }

  private Map<String, List<ServiceInstanceView>> getAllServiceInstanceFilterByApp(
      String serviceName, String app, String node) {

    Map<String, List<ServiceInstanceView>> viewMaps = new HashMap<>();
    ConcurrentHashMap<String, ServiceProviderDescriptor> descs =
        serviceDescriptors.get(serviceName);
    if (descs == null)
      return viewMaps;
    for (Entry<String, ServiceProviderDescriptor> desc : descs.entrySet()) {
      if (StringUtils.isNotBlank(app) && !StringUtils.equals(app, desc.getValue().getApp())) {
        continue;
      }
      if (StringUtils.isNotBlank(node) && !StringUtils.equals(node, desc.getValue().getHost())) {
        continue;
      }
      ServiceInstanceView view = convert(desc.getKey(), desc.getValue());
      String key =
          Utils.generateKey(view.getVersion(), view.getGroup(), view.getProtocol(), view.getApp());
      List<ServiceInstanceView> mapViews = viewMaps.get(key);
      if (mapViews == null) {
        mapViews = new ArrayList<>();
        viewMaps.put(key, mapViews);
      }
      mapViews.add(view);
    }
    return viewMaps;
  }



  private ServiceInstanceView convert(String key, ServiceProviderDescriptor desc) {
    ServiceInstanceView view = new ServiceInstanceView();
    view.setApp(desc.getApp());
    view.setHost(desc.getHost());
    view.setPort(desc.getPort());
    view.setProtocol(desc.getProtocol());
    view.setRegistTime(new Date(desc.getRegistTime()));
    view.setReturnType(desc.getReturnType());
    view.setServiceName(desc.getName());
    view.setVersion(desc.getVersion());
    view.setParamTypes(desc.getParamTypes());
    view.setKey(key);
    view.setGroup(desc.getGroup());
    EndpointConfig config = desc.getConfig();
    String configStr = config.toString();
    view.setConfig(configStr);
    view.setForceAccess(desc.isForceAccess());
    view.setHidden(desc.isHidden());
    view.setInterceptors(desc.getConfig().get(ConfigConstans.PROVIDER_INTERCEPTORS));
    view.setMaxConcurrnet(
        desc.getConfig().getValueAsInt(ConfigConstans.PROVIDER_MAX_CONCURRENT, -1));
    view.setThreadpoolNum(
        desc.getConfig().getValueAsInt(ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE, 0));
    view.setUseThreadpool(
        desc.getConfig().getValueAsBoolean(ConfigConstans.PROVIDER_DISPATCHER_THREAD_EXE, false)
            ? false : true);
    return view;

  }

  public Map<String, List<ReferInstanceView>> getAllReferInstance(String serviceName) {

    return getAllReferInstanceFilterByApp(serviceName, null, null);
  }

  public Map<String, List<ReferInstanceView>> getAllReferInstanceFilterByApp(String serviceName,
      String app, String node) {
    Map<String, List<ReferInstanceView>> viewMaps = new HashMap<>();

    ConcurrentHashMap<String, ServiceReferenceDescriptor> descs = referDescriptors.get(serviceName);
    if (descs == null)
      return viewMaps;
    for (Entry<String, ServiceReferenceDescriptor> desc : descs.entrySet()) {
      if (StringUtils.isNotBlank(app) && !StringUtils.equals(app, desc.getValue().getReferApp())) {
        continue;
      }
      if (StringUtils.isNotBlank(node) && !StringUtils.equals(node, desc.getValue().getHost())) {
        continue;
      }
      ReferInstanceView view = convert(desc.getKey(), desc.getValue());
      String key = Utils.generateKey(view.getReferVersion(), view.getReferGroup(),
          view.getProtocol(), view.getReferApp());
      List<ReferInstanceView> mapViews = viewMaps.get(key);
      if (mapViews == null) {
        mapViews = new ArrayList<>();
        viewMaps.put(key, mapViews);
      }
      mapViews.add(view);
    }
    return viewMaps;
  }

  private ReferInstanceView convert(String key, ServiceReferenceDescriptor desc) {
    ReferInstanceView view = new ReferInstanceView();
    view.setProtocol(desc.getProtocol());
    view.setReferApp(desc.getReferApp());
    view.setReferHost(desc.getHost());
    view.setReferVersion(desc.getReferVersion());
    view.setRegistTime(new Date(desc.getRegistTime()));
    view.setServiceName(desc.getServiceName());
    view.setReturnType(desc.getReturnType());
    view.setParamTypes(desc.getParamTypes());
    view.setReferGroup(desc.getReferGroup());
    view.setKey(key);
    return view;

  }

  public Map<String, List<ServiceInstanceView>> getAppServiceList(String app) {
    Vertex node = graph.getVertex(app);
    if (node == null)
      return null;

    Set<String> snames = node.getProperty("serviceName");
    if (snames == null)
      return null;

    Map<String, List<ServiceInstanceView>> rst = new HashMap<>();
    for (String sr : snames) {
      Map<String, List<ServiceInstanceView>> allviews =
          getAllServiceInstanceFilterByApp(sr, app, null);
      if (allviews != null) {
        rst.putAll(allviews);
      }
    }
    return rst;
  }

  public Map<String, List<ReferInstanceView>> getAppReferList(String app) {
    Vertex node = graph.getVertex(app);
    if (node == null)
      return null;

    Set<String> snames = node.getProperty("referName");
    if (snames == null)
      return null;

    Map<String, List<ReferInstanceView>> rst = new HashMap<>();
    for (String sr : snames) {
      Map<String, List<ReferInstanceView>> allviews = getAllReferInstanceFilterByApp(sr, app, null);
      if (allviews != null) {
        rst.putAll(allviews);
      }
    }
    return rst;
  }

  public Collection<ServiceProviderDescriptor> searchServiceInstance(String app, String serviceName,
      String version, String host, int port, String protocol) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getNodeApps(String node) {
    Vertex nodeVertexService = nodeGraph.getVertex(node);
    if (nodeVertexService == null)
      return null;
    return nodeVertexService.getProperty("apps");
  }

}
