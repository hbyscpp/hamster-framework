package com.seaky.hamster.core.rpc.registeration;


import org.apache.commons.lang.StringUtils;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;

/**
 * 
 * 服务的描述
 * 
 * @author seaky
 * @since 1.0.0
 * @Date 2015年12月9日
 */
public class ServiceProviderDescriptor {

  // 服务的配置
  private EndpointConfig config = new EndpointConfig();

  /**
   * @return the name
   */
  public String getName() {
    return config.get(ConfigConstans.PROVIDER_NAME);
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_NAME, name));
  }

  /**
   * @return the protocol
   */
  public String getProtocol() {
    return config.get(ConfigConstans.PROVIDER_PROTOCOL);
  }

  /**
   * @param protocol the protocol to set
   */
  public void setProtocol(String protocol) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_PROTOCOL, protocol));
  }

  /**
   * @return the app
   */
  public String getApp() {
    return this.config.get(ConfigConstans.PROVIDER_APP);
  }

  public void setApp(String app) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_APP, app));
  }

  public String getPid() {
    return this.config.get(ConfigConstans.PROVIDER_PID);
  }

  public void setPid(String pid) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_PID, pid));
  }

  public String toString() {
    return config.toString();
  }

  /**
   * @return the host
   */
  public String getHost() {
    return this.config.get(ConfigConstans.PROVIDER_HOST);
  }

  /**
   * @param host the host to set
   */
  public void setHost(String host) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_HOST, host));
  }

  /**
   * @param port the port to set
   */
  public void setPort(int port) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_PORT, String.valueOf(port)));
  }

  /**
   * @param version the version to set
   */
  public String getVersion() {
    return this.config.get(ConfigConstans.PROVIDER_VERSION);
  }

  public void setVersion(String version) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_VERSION, version));
  }

  public String getGroup() {
    return this.config.get(ConfigConstans.PROVIDER_GROUP);
  }

  public void setGroup(String group) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_GROUP, group));
  }


  public int getPort() {
    return this.config.getValueAsInt(ConfigConstans.PROVIDER_PORT, -1);
  }

  public void setParamTypes(String[] params) {
    this.config
        .addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_PARAMS, paramsToString(params)));
  }

  public String[] getParamTypes() {

    String parms = this.config.get(ConfigConstans.PROVIDER_PARAMS, "");
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
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_RETURN, returnType));
  }

  public String getReturnType() {
    return this.config.get(ConfigConstans.PROVIDER_RETURN);
  }

  public static ServiceProviderDescriptor parseStr(String str) {
    if (StringUtils.isBlank(str))
      throw new RuntimeException("error sd format " + str);
    ServiceProviderDescriptor sd = new ServiceProviderDescriptor();
    EndpointConfig ec = EndpointConfig.parseStr(str);
    sd.setConfig(ec);
    return sd;
  }

  public EndpointConfig getConfig() {
    return config;
  }

  public void setConfig(EndpointConfig config) {
    this.config = config;
  }

  public boolean isHidden() {
    return this.config.getValueAsBoolean(ConfigConstans.PROVIDER_HIDDERN, false);
  }

  public long getRegistTime() {
    return this.config.getValueAsLong(ConfigConstans.PROVIDER_REG_TIME, 0L);
  }

  public void setRegistTime(long registTime) {
    this.config.addConfigItem(
        new ConfigItem(ConfigConstans.PROVIDER_REG_TIME, String.valueOf(registTime)));
  }

  public boolean isForceAccess() {
    return this.config.getValueAsBoolean(ConfigConstans.PROVIDER_FORCE_ACCESS, false);
  }


  public void setFrameworkVersion(String version) {
    this.config.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_FRAMEWORK_VERSION, version));
  }
}
