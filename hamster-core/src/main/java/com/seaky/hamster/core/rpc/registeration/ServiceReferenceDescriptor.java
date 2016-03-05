package com.seaky.hamster.core.rpc.registeration;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;

// 服务的引用描述
public class ServiceReferenceDescriptor {

  // 具体的配置
  private EndpointConfig config;

  // 机器地址对
  private ConcurrentHashMap<String, String> addressPairs = new ConcurrentHashMap<String, String>();

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
    config.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_REG_TIME, String
        .valueOf(registTime), true));
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

  public Set<String> getAddressPairs() {
    return addressPairs.keySet();
  }

  public boolean containPair(String pair) {
    return addressPairs.containsKey(pair);
  }

  public void addAddressPair(String addressPair) {
    addressPairs.put(addressPair, "");
  }

  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append(config.toString());
    sb.append(Constants.TILDE_LINE);
    sb.append(hostPairsString());
    return sb.toString();

  }

  private String hostPairsString() {

    StringBuilder sb = new StringBuilder();
    for (String pair : addressPairs.keySet()) {
      sb.append(pair).append(Constants.COMMA);
    }
    return sb.toString();
  }

  public static ServiceReferenceDescriptor parseStr(String str) {
    if (StringUtils.isBlank(str))
      throw new RuntimeException("error sd format " + str);

    String[] params = str.split(Constants.TILDE_LINE, -1);
    if (params == null || params.length != 3)
      throw new RuntimeException("error sd format " + str);


    ServiceReferenceDescriptor rd = new ServiceReferenceDescriptor();
    setAddressPair(params[2], rd);
    EndpointConfig ec = EndpointConfig.parseStr(params[0], params[1]);
    rd.setConfig(ec);
    return rd;
  }

  private static void setAddressPair(String addressPairs, ServiceReferenceDescriptor rd) {
    if (StringUtils.isBlank(addressPairs))
      return;

    String[] pairs = addressPairs.split(Constants.COMMA);

    if (pairs != null) {
      for (String pair : pairs) {
        rd.addressPairs.put(pair, "");
      }
    }
  }


}
