package com.seaky.hamster.spring;

import java.util.HashMap;
import java.util.Map;

public class ClassReferenceConfig extends ReferenceConfig {


  private String clientId;


  private Map<String, ReferenceConfig> configs = new HashMap<String, ReferenceConfig>();



  public void addConfig(String name, ReferenceConfig config) {
    configs.put(name, config);
  }


  public String getClientId() {
    return clientId;
  }


  public void setClientId(String clientId) {
    this.clientId = clientId;
  }


}
