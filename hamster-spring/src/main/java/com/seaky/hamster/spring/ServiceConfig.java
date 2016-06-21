package com.seaky.hamster.spring;

public class ServiceConfig {

  private String app;

  private String version = "1.0.0";

  private String group = "default";

  // 并发控制
  private int maxConcurrent = -1;

  // 异常转换扩展
  private String exceptionConvertor = "default";

  // 是否使用dispatcher线程执行服务
  private boolean useDispatcherThreadExe = false;

  // 线程池的名字
  private String threadPoolName = null;

  // 是否隐藏，隐藏的服务只能通过直连来访问，用于测试
  private Boolean isHidden = null;

  private boolean isForceAccess = false;

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

  public String getExceptionConvertor() {
    return exceptionConvertor;
  }

  public void setExceptionConvertor(String exceptionConvertor) {
    this.exceptionConvertor = exceptionConvertor;
  }

  /**
   * @return the useDispatcherThreadExe
   */
  public boolean isUseDispatcherThreadExe() {
    return useDispatcherThreadExe;
  }

  /**
   * @param useDispatcherThreadExe the useDispatcherThreadExe to set
   */
  public void setUseDispatcherThreadExe(boolean useDispatcherThreadExe) {
    this.useDispatcherThreadExe = useDispatcherThreadExe;
  }

  /**
   * @return the threadPoolName
   */
  public String getThreadPoolName() {
    return threadPoolName;
  }

  /**
   * @param threadPoolName the threadPoolName to set
   */
  public void setThreadPoolName(String threadPoolName) {
    this.threadPoolName = threadPoolName;
  }

  public Boolean isHidden() {
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



}
