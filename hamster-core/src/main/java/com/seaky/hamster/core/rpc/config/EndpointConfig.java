/**
 * Copyright (c) @2016,重庆云途交通科技有限公司.版权所有
 */
package com.seaky.hamster.core.rpc.config;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.seaky.hamster.core.rpc.common.Constants;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Feb 27, 2016
 */
public class EndpointConfig {

  private ConcurrentHashMap<String, ConfigItem> configs =
      new ConcurrentHashMap<String, ConfigItem>();

  public void addConfigItem(ConfigItem item) {
    addConfigItem(item, true);
  }

  public boolean addConfigItem(ConfigItem item, boolean isOverride) {
    if (item == null || item.getKey() == null || item.getValue() == null)
      throw new IllegalArgumentException();
    if (!isOverride) {
      ConfigItem ci = configs.putIfAbsent(item.getKey(), item);
      return ci == null;
    } else {
      configs.put(item.getKey(), item);
      return true;
    }
  }

  public void remove(String key) {
    configs.remove(key);
  }

  public void remove(ConfigItem item) {
    remove(item.getKey());
  }

  public String get(String key) {
    return get(key, null);
  }

  public String get(String key, String defaultValue) {
    if (key == null)
      throw new IllegalArgumentException();
    ConfigItem item = configs.get(key);
    if (item == null)
      return defaultValue;
    return item.getValue();
  }

  public int getValueAsInt(String key, int defaultValue) {

    String value = get(key);
    if (value == null)
      return defaultValue;
    return Integer.valueOf(value);
  }

  public long getValueAsLong(String key, long defaultValue) {
    String value = get(key);
    if (value == null)
      return defaultValue;
    return Long.valueOf(value);
  }

  public double getValueAsDouble(String key, double defaultValue) {
    String value = get(key);
    if (value == null)
      return defaultValue;
    return Double.valueOf(value);
  }

  public boolean getValueAsBoolean(String key, boolean defaultValue) {
    String value = get(key);
    if (value == null)
      return defaultValue;
    return Boolean.valueOf(value);
  }

  public EndpointConfig deepCopy() {
    EndpointConfig cf = new EndpointConfig();

    for (ConfigItem ci : configs.values()) {
      cf.addConfigItem(ci);
    }
    return cf;
  }

  public String toString() {
    StringBuilder rstr = new StringBuilder();
    StringBuilder rwstr = new StringBuilder();

    for (ConfigItem ci : configs.values()) {
      if (ci.isReadOnly()) {
        rstr.append(ci.getKey()).append(Constants.EQUAL).append(ci.getValue())
            .append(Constants.AND);
      } else {
        rwstr.append(ci.getKey()).append(Constants.EQUAL).append(ci.getValue())
            .append(Constants.AND);

      }
    }

    return rstr.append(Constants.TILDE_LINE).append(rwstr).toString();
  }

  public static EndpointConfig parseStr(String value) {
    if (StringUtils.isBlank(value)) {
      return parseStr("", "");
    }
    String[] params = value.split(Constants.TILDE_LINE, -1);

    if (params.length != 2)
      throw new IllegalArgumentException();

    return parseStr(params[0], params[1]);

  }

  public static EndpointConfig parseStr(String rstr, String rwstr) {
    EndpointConfig ec = new EndpointConfig();

    if (StringUtils.isNotBlank(rstr)) {
      String[] basicAttrs = rstr.split(Constants.AND);

      for (String basicAttr : basicAttrs) {
        String[] kv = basicAttr.split(Constants.EQUAL, -1);
        ec.addConfigItem(new ConfigItem(kv[0], kv[1], true));
      }
    }
    if (StringUtils.isNotBlank(rwstr)) {
      String[] basicAttrs = rwstr.split(Constants.AND);

      for (String basicAttr : basicAttrs) {
        String[] kv = basicAttr.split(Constants.EQUAL, -1);
        ec.addConfigItem(new ConfigItem(kv[0], kv[1], false));
      }
    }
    return ec;
  }

  public void mergeServiceConfig(EndpointConfig commonConfig) {

    for (ConfigItem ci : commonConfig.configs.values()) {
      this.addConfigItem(ci, false);
    }

  }
}
