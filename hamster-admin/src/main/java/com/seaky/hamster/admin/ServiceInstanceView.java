package com.seaky.hamster.admin;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ServiceInstanceView {


  private String key;

  private String app;

  private String version;

  private String[] paramTypes;

  private String returnType;

  private String host;

  private String protocol;

  private int port;

  private String group;

  private String serviceName;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date registTime;

  // 是否启用线程池
  private boolean isUseThreadpool;

  // 线程池大小
  private int threadpoolNum;

  // 最大并发
  private int maxConcurrnet;

  // 是否隐藏
  private boolean isHidden;

  // 是否强制访问
  private boolean isForceAccess;

  // 服务所有的属性
  private String config;

  private String pid;

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
   * @return the paramTypes
   */
  public String[] getParamTypes() {
    return paramTypes;
  }

  /**
   * @param paramTypes the paramTypes to set
   */
  public void setParamTypes(String[] paramTypes) {
    this.paramTypes = paramTypes;
  }

  /**
   * @return the returnType
   */
  public String getReturnType() {
    return returnType;
  }

  /**
   * @param returnType the returnType to set
   */
  public void setReturnType(String returnType) {
    this.returnType = returnType;
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
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port the port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the host to set
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

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

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
  }

  public boolean isUseThreadpool() {
    return isUseThreadpool;
  }

  public void setUseThreadpool(boolean isUseThreadpool) {
    this.isUseThreadpool = isUseThreadpool;
  }

  public int getThreadpoolNum() {
    return threadpoolNum;
  }

  public void setThreadpoolNum(int threadpoolNum) {
    this.threadpoolNum = threadpoolNum;
  }

  public int getMaxConcurrnet() {
    return maxConcurrnet;
  }

  public void setMaxConcurrnet(int maxConcurrnet) {
    this.maxConcurrnet = maxConcurrnet;
  }

  public boolean isHidden() {
    return isHidden;
  }

  public void setHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }

  public boolean isForceAccess() {
    return isForceAccess;
  }

  public void setForceAccess(boolean isForceAccess) {
    this.isForceAccess = isForceAccess;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getPid() {
    return pid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }


}
