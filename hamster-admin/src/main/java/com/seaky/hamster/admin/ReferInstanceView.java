package com.seaky.hamster.admin;

import java.util.Date;

public class ReferInstanceView {

  private String referApp;

  private String serviceName;

  private String referVersion;

  private String referHost;

  private String referGroup;

  private String protocol;
  // 服务端的app

  private Date registTime;

  private String[] paramTypes;

  private String returnType;

  private String key;

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
    this.serviceName = serviceName;
  }


  /**
   * @return the protocol
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * @param protocol the protocol to set
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
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
   * @return the clientHost
   */
  public String getReferHost() {
    return referHost;
  }

  /**
   * @param clientHost the clientHost to set
   */
  public void setReferHost(String clientHost) {
    this.referHost = clientHost;
  }


  /**
   * @return the registTime
   */
  public Date getRegistTime() {
    return registTime;
  }

  /**
   * @param registTime the registTime to set
   */
  public void setRegistTime(Date registTime) {
    this.registTime = registTime;
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

  public String[] getParamTypes() {
    return paramTypes;
  }

  public void setParamTypes(String[] paramTypes) {
    this.paramTypes = paramTypes;
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public String getReferGroup() {
    return referGroup;
  }

  public void setReferGroup(String referGroup) {
    this.referGroup = referGroup;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

}
