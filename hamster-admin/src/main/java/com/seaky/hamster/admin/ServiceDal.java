package com.seaky.hamster.admin;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.seaky.hamster.core.rpc.config.EndpointConfig;

public class ServiceDal {

  public static ServiceDal dal = new ServiceDal("localhost:2181", "localhost:2181");



  public ServiceDal(String registerurl, String configurl) {}

  public Collection<String> allServiceName() {
    return null;
  }

  public Map<String, String> getServiceConfig(String app, String serviceName, String key) {
    return null;

  }

  public void updateServiceConfig(String app, String serviceName, String key,
      Map<String, String> configMap) {

    if (StringUtils.isBlank(app) || StringUtils.isBlank(serviceName))
      throw new RuntimeException("app or service name can not be null");
    EndpointConfig sc = new EndpointConfig();
    if (configMap != null) {
      for (Entry<String, String> entry : configMap.entrySet()) {
        if (StringUtils.isNotBlank(entry.getKey())) {
          // sc.put(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  public Map<String, String> getReferConfig(String app, String serviceName, String key) {
    return null;

  }

  public void updateReferConfig(String app, String serviceName, String key,
      Map<String, String> configMap) {

  }

  public Collection<ReferInstanceView> allReferInstance(String serviceName) {

    return null;

  }

  public Collection<ServiceInstanceView> allServiceInstance(String serviceName) {


    return null;
  }

  public Collection<ServiceInstanceView> searchService(String app, String serviceName,
      String version, String host, int port, String protocol) {


    return null;
  }
}
