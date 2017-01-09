package com.seaky.hamster.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClassExportConfig extends ServiceConfig {


  private String serverId = "default";

  private String beanId;

  private Map<String, ServiceConfig> configs = new HashMap<String, ServiceConfig>();



  public void addMethodConfig(String name, ServiceConfig config) {
    configs.put(name, config);
  }

  public Map<String, ServiceConfig> getMethodConfigs() {
    return Collections.unmodifiableMap(configs);
  }


  public String getBeanId() {
    return beanId;
  }


  public void setBeanId(String beanId) {
    this.beanId = beanId;
  }


  public String getServerId() {
    return serverId;
  }


  public void setServerId(String serverId) {
    this.serverId = serverId;
  }


}
