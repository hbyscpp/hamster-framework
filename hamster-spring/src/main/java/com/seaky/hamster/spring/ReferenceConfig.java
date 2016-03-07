package com.seaky.hamster.spring;

public class ReferenceConfig {

  private String app;

  private String version = "1.0.0";

  private String group = "default";

  private int maxConcurrent = -1;

  private int readtimeout = 1000;

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


}
