package com.seaky.hamster.core.rpc.config;

import org.apache.commons.lang.StringUtils;

import com.seaky.hamster.core.rpc.common.Constants;



// 配置项目
public class ConfigItem {

  private String key;

  // value为null 不会加入到配置之中
  private String value;

  public ConfigItem(String key, String value) {

    if (StringUtils.containsAny(key, Constants.CONFIG_KEY_NOT_ALLOW_STR)
        || StringUtils.containsAny(value, Constants.CONFIG_VALUE_NOT_ALLOW_STR)) {
      throw new IllegalArgumentException("key or value contain invalid str");
    }

    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

}
