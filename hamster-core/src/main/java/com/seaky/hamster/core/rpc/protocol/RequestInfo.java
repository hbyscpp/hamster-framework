package com.seaky.hamster.core.rpc.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 参数的描述
public class RequestInfo {

  // 请求服务所在的应用
  private String app;
  // 请求的来源的应用
  private String referApp;
  // 服务的名字
  private String serviceName;

  // 想要引用的group
  private String referGroup;
  // 实际的group
  private String group;
  // 请求的来源的版本
  private String referVersion;
  // 请求服务的版本
  private String version;
  // 附加属性，用于一些特性的实现 比如序列号，监控等,应用应该避免设置
  private Map<String, String> attachments = new ConcurrentHashMap<String, String>();
  // 请求参数
  private Object[] params;


  public RequestInfo(String app, String referApp, String serviceName, String version,
      String referVersion, String group, String referGroup, Object[] params) {
    this.setApp(app);
    this.referApp = referApp;
    this.serviceName = serviceName;
    this.referVersion = referVersion;
    this.version = version;
    this.group = group;
    this.referGroup = referGroup;
    this.params = params;
  }

  public void addAttachment(String key, String value) {
    this.attachments.put(key, value);
  }

  public void addAttachment(Map<String, String> otherAttach) {
    if (otherAttach != null) {
      this.attachments.putAll(otherAttach);
    }
  }

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @return the attachments
   */
  public Map<String, String> getAttachments() {
    return attachments;
  }

  /**
   * @param attachments the attachments to set
   */
  public void setAttachments(Map<String, String> attachments) {
    this.attachments = attachments;
  }

  public String getAttachment(String key) {
    if (attachments == null)
      return null;
    return attachments.get(key);
  }

  /**
   * @return the app
   */
  public String getApp() {
    return app;
  }

  /**
   * @return the params
   */
  public Object[] getParams() {
    return params;
  }

  /**
   * @param params the params to set
   */
  public void setParams(Object[] params) {
    this.params = params;
  }


  /**
   * @return the referApp
   */
  public String getReferApp() {
    return referApp;
  }

  /**
   * @return the referVersion
   */
  public String getReferVersion() {
    return referVersion;
  }

  /**
   * @param referVersion the referVersion to set
   */
  public void setReferVersion(String referVersion) {
    this.referVersion = referVersion;
  }

  /**
   * @return the serviceVersion
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param serviceVersion the serviceVersion to set
   */
  public void setVersion(String serviceVersion) {
    this.version = serviceVersion;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getReferGroup() {
    return referGroup;
  }

  public void setReferGroup(String referGroup) {
    this.referGroup = referGroup;
  }

  public void setApp(String app) {
    this.app = app;
  }

}
