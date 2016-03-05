package com.seaky.hamster.core.rpc.protocol;

import com.seaky.hamster.core.rpc.config.EndpointConfig;

public class ReferenceInfo {

  // 需引用的服务名
  private String serviceName;
  // 引用所在的app
  private String referApp;
  // 引用的version
  private String version;

  private String group;

  private EndpointConfig config;

  public ReferenceInfo(String serviceName, String referApp, String version, String group,
      EndpointConfig config) {
    this.serviceName = serviceName;
    this.referApp = referApp;
    this.version = version;
    this.setConfig(config);
    this.setGroup(group);
  }

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @return the referApp
   */
  public String getReferApp() {
    return referApp;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public EndpointConfig getConfig() {
    return config;
  }

  public void setConfig(EndpointConfig config) {
    this.config = config;
  }



}
