package com.seaky.hamster.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClassReferenceConfig extends ReferenceConfig {


  private String clientId = "default";


  private Map<String, ReferenceConfig> configs = new HashMap<String, ReferenceConfig>();



  public void addMethodConfig(String name, ReferenceConfig config) {
    configs.put(name, config);
  }

  public Map<String, ReferenceConfig> getMethodConfigs() {
    return Collections.unmodifiableMap(configs);
  }


  public String getClientId() {
    return clientId;
  }


  public void setClientId(String clientId) {
    this.clientId = clientId;
  }


}
