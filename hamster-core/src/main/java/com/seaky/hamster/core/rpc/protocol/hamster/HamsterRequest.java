package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

// 请求的信息
// 编码
public class HamsterRequest {

  // 服务提供者的应用
  private String app;
  // 服务消费者的应用
  private String referApp;
  // 服务提供者的配置的key
  private String group;
  // 服务消费者的配置的key
  private String referGroup;
  // 服务消费者请求的版本
  private String referVersion;
  // 服务提供者提供的版本
  private String serviceVersion;

  // 服务名字
  private String serviceName;
  // 参数
  private byte[] params;

  // 附加信息，用于框架或者扩展
  private Map<String, String> attachments;

  /**
   * @return the body
   */



  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @param serviceName the serviceName to set
   */
  public void setServiceName(String serviceName) {
    if (StringUtils.isBlank(serviceName))
      throw new RuntimeException("service name can not be null");
    this.serviceName = serviceName;
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

  public void addAttachment(String key, String value) {
    if (attachments == null)
      attachments = new HashMap<String, String>();
    attachments.put(key, value);
  }

  /**
   * @return the referApp
   */
  public String getReferApp() {
    return referApp;
  }

  /**
   * @param referApp the referApp to set
   */
  public void setReferApp(String referApp) {
    this.referApp = referApp;
  }

  /**
   * @return the app
   */
  public String getApp() {
    return app;
  }

  /**
   * @param app the app to set
   */
  public void setApp(String app) {
    this.app = app;
  }



  /**
   * @return the params
   */
  public byte[] getParams() {
    return params;
  }

  /**
   * @param params the params to set
   */
  public void setParams(byte[] params) {
    this.params = params;
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
  public String getServiceVersion() {
    return serviceVersion;
  }

  /**
   * @param serviceVersion the serviceVersion to set
   */
  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = serviceVersion;
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



}
