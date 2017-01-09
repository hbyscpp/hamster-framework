package com.seaky.hamster.core.rpc.protocol;

public class ProtocolRequestHeader {

  private String app;

  private String referenceApp;

  private String group;

  private String referenceGroup;

  private String version;

  private String referenceVersion;

  private String serviceName;

  private Attachments attachments = new Attachments();


  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public String getReferenceApp() {
    return referenceApp;
  }

  public void setReferenceApp(String referenceApp) {
    this.referenceApp = referenceApp;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getReferenceGroup() {
    return referenceGroup;
  }

  public void setReferenceGroup(String referenceGroup) {
    this.referenceGroup = referenceGroup;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getReferenceVersion() {
    return referenceVersion;
  }

  public void setReferenceVersion(String referenceVersion) {
    this.referenceVersion = referenceVersion;
  }

  public Attachments getAttachments() {
    return attachments;
  }

  public void setAttachments(Attachments attachments) {
    this.attachments = attachments;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

}
