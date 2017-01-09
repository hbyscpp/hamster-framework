package com.seaky.hamster.core.rpc.registeration;

import org.apache.commons.lang.StringUtils;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;

// 服务的引用描述
public class ServiceReferenceDescriptor {

  // 具体的配置
  private EndpointConfig config = new EndpointConfig();

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return config.get(ConfigConstans.REFERENCE_NAME);
  }

  /**
   * @param serviceName the serviceName to set
   */
  public void setServiceName(String serviceName) {

    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_NAME, serviceName, true));
  }

  /**
   * @return the protocol
   */
  public String getProtocol() {
    return config.get(ConfigConstans.REFERENCE_PROTOCOL);
  }

  /**
   * @param protocol the protocol to set
   */
  public void setProtocol(String protocol) {
    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_PROTOCOL, protocol, true));
  }

  /**
   * @return the referApp
   */
  public String getReferApp() {
    return config.get(ConfigConstans.REFERENCE_APP);
  }

  /**
   * @param referApp the referApp to set
   */
  public void setReferApp(String referApp) {
    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_APP, referApp, true));
  }

  public String getHost() {

    return config.get(ConfigConstans.REFERENCE_HOST);
  }

  /**
   * @param registTime the registTime to set
   */
  public void setHost(String host) {
    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_HOST, host, true));
  }

  /**
   * @return the registTime
   */
  public long getRegistTime() {

    return config.getValueAsLong(ConfigConstans.REFERENCE_REG_TIME, 0L);
  }

  /**
   * @param registTime the registTime to set
   */
  public void setRegistTime(long registTime) {
    config.addConfigItem(
        new ConfigItem(ConfigConstans.REFERENCE_REG_TIME, String.valueOf(registTime), true));
  }

  /**
   * @return the referVersion
   */
  public String getReferVersion() {
    return config.get(ConfigConstans.REFERENCE_VERSION);
  }

  /**
   * @param referVersion the referVersion to set
   */
  public void setReferVersion(String referVersion) {
    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_VERSION, referVersion, true));
  }

  public EndpointConfig getConfig() {
    return config;
  }

  public void setConfig(EndpointConfig config) {
    this.config = config;
  }

  public String getPid() {
    return config.get(ConfigConstans.REFERENCE_PID);
  }

  public void setPid(String pid) {
    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_PID, pid, true));
  }

  public String getReferGroup() {
    return config.get(ConfigConstans.REFERENCE_GROUP);
  }

  public void setReferGroup(String referGroup) {
    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_GROUP, referGroup, true));
  }

  public void setParamTypes(String[] params) {
    this.config.addConfigItem(
        new ConfigItem(ConfigConstans.REFERENCE_PARAMS, paramsToString(params), true));
  }

  public String[] getParamTypes() {
    String parms = this.config.get(ConfigConstans.REFERENCE_PARAMS, "");
    if (StringUtils.isBlank(parms))
      return null;
    return parms.split(Constants.COMMA);
  }

  private String paramsToString(String[] paramTypes) {
    StringBuilder sb = new StringBuilder();
    if (paramTypes != null && paramTypes.length > 0) {
      for (String p : paramTypes) {
        sb.append(p).append(Constants.COMMA);
      }
    }
    return sb.toString();
  }

  public void setReturnType(String returnType) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_RETURN, returnType, true));
  }

  public String getReturnType() {
    return this.config.get(ConfigConstans.REFERENCE_RETURN);
  }

  public String toString() {

    return config.toString();

  }

  public void setFrameworkVersion(String version) {
    this.config
        .addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_FRAMEWORK_VERSION, version, true));
  }


  public static ServiceReferenceDescriptor parseStr(String str) {
    if (StringUtils.isBlank(str))
      throw new RuntimeException("error sd format " + str);
    ServiceReferenceDescriptor rd = new ServiceReferenceDescriptor();
    EndpointConfig ec = EndpointConfig.parseStr(str);
    rd.setConfig(ec);
    return rd;
  }

}
