package com.seaky.hamster.spring;

import com.seaky.hamster.core.rpc.config.ConfigConstans;

public class ReferenceConfig {

  private String app;

  private String version = "1.0.0";

  private String group = "default";

  private int maxConcurrent = -1;

  private int readtimeout = ConfigConstans.REFERENCE_READ_TIMEOUT_DEFAULT;

  private int callTimeout = ConfigConstans.REFERENCE_CALL_TIMEOUT_DEFAULT;

  private String exceptionConvertor = "default";

  private String providerAddresses;

  private String interceptors;

  // 是否线程池执行
  private boolean isUseThreadPoolExe = false;

  private Integer theadpoolMaxQueueSize = ConfigConstans.REFERENCE_THREADPOOL_MAXQUEUE_DEFAULT;

  private Integer theadpoolSize = ConfigConstans.REFERENCE_THREADPOOL_MAXSIZE_DEFAULT;

  private String loadbalancer = null;

  private String cluster = null;

  private String router = null;

  private String serializerName = null;

  private int circuitbreakerOpenFailNumber =
      ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_FAIL_NUMBER_DEFAULT;

  private int circuitbreakerOpenTotalNumber =
      ConfigConstans.REFERENCE_CIRCUITBREAKER_OPEN_TOTAL_NUMBER_DEFAULT;

  private int circuitbreakerCloseSuccessNumber =
      ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_SUCCESS_NUMBER_DEFAULT;

  private int circuitbreakerCloseTotalNumber =
      ConfigConstans.REFERENCE_CIRCUITBREAKER_CLOSE_TOTAL_NUMBER_DEFAULT;

  private int circuitbreakerHalfopenDelay =
      ConfigConstans.REFERENCE_CIRCUITBREAKER_HALFOPEN_DELAY_DEFAULT;;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public int getMaxConcurrent() {
    return maxConcurrent;
  }

  public void setMaxConcurrent(int maxConcurrent) {
    this.maxConcurrent = maxConcurrent;
  }

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public int getReadtimeout() {
    return readtimeout;
  }

  public void setReadtimeout(int readtimeout) {
    this.readtimeout = readtimeout;
  }

  public String getExceptionConvertor() {
    return exceptionConvertor;
  }

  public void setExceptionConvertor(String exceptionConvertor) {
    this.exceptionConvertor = exceptionConvertor;
  }

  public String getProviderAddresses() {
    return providerAddresses;
  }

  public void setProviderAddresses(String providerAddresses) {
    this.providerAddresses = providerAddresses;
  }

  public String getInterceptors() {
    return interceptors;
  }

  public void setInterceptors(String interceptors) {
    this.interceptors = interceptors;
  }


  public int getCircuitbreakerOpenTotalNumber() {
    return circuitbreakerOpenTotalNumber;
  }

  public void setCircuitbreakerOpenTotalNumber(int circuitbreakerOpenTotalNumber) {
    this.circuitbreakerOpenTotalNumber = circuitbreakerOpenTotalNumber;
  }

  public int getCircuitbreakerCloseTotalNumber() {
    return circuitbreakerCloseTotalNumber;
  }

  public void setCircuitbreakerCloseTotalNumber(int circuitbreakerCloseTotalNumber) {
    this.circuitbreakerCloseTotalNumber = circuitbreakerCloseTotalNumber;
  }

  public int getCircuitbreakerCloseSuccessNumber() {
    return circuitbreakerCloseSuccessNumber;
  }

  public void setCircuitbreakerCloseSuccessNumber(int circuitbreakerCloseSuccessNumber) {
    this.circuitbreakerCloseSuccessNumber = circuitbreakerCloseSuccessNumber;
  }

  public int getCircuitbreakerHalfopenDelay() {
    return circuitbreakerHalfopenDelay;
  }

  public void setCircuitbreakerHalfopenDelay(int circuitbreakerHalfopenDelay) {
    this.circuitbreakerHalfopenDelay = circuitbreakerHalfopenDelay;
  }

  public int getCallTimeout() {
    return callTimeout;
  }

  public void setCallTimeout(int callTimeout) {
    this.callTimeout = callTimeout;
  }

  public int getCircuitbreakerOpenFailNumber() {
    return circuitbreakerOpenFailNumber;
  }

  public void setCircuitbreakerOpenFailNumber(int circuitbreakerOpenFailNumber) {
    this.circuitbreakerOpenFailNumber = circuitbreakerOpenFailNumber;
  }

  public boolean isUseThreadPoolExe() {
    return isUseThreadPoolExe;
  }

  public void setUseThreadPoolExe(boolean isUseThreadPoolExe) {
    this.isUseThreadPoolExe = isUseThreadPoolExe;
  }


  public String getSerializerName() {
    return serializerName;
  }

  public void setSerializerName(String serializerName) {
    this.serializerName = serializerName;
  }

  public Integer getTheadpoolMaxQueueSize() {
    return theadpoolMaxQueueSize;
  }

  public void setTheadpoolMaxQueueSize(Integer theadpoolMaxQueueSize) {
    this.theadpoolMaxQueueSize = theadpoolMaxQueueSize;
  }

  public Integer getTheadpoolSize() {
    return theadpoolSize;
  }

  public void setTheadpoolSize(Integer theadpoolSize) {
    this.theadpoolSize = theadpoolSize;
  }

  public String getLoadbalancer() {
    return loadbalancer;
  }

  public void setLoadbalancer(String loadbalancer) {
    this.loadbalancer = loadbalancer;
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public String getRouter() {
    return router;
  }

  public void setRouter(String router) {
    this.router = router;
  }


}
