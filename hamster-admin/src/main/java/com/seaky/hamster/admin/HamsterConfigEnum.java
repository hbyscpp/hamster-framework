package com.seaky.hamster.admin;

import java.util.HashMap;
import java.util.Map;

public enum HamsterConfigEnum {

  App("app", "应用"), Version("version", "版本"), Params("paramTypes", "参数类型"), ReturnType("returnType",
      "返回类型"), Host("host", "节点"), Port("port", "端口"), RegTime("registTime", "注册时间"), ReferApp(
          "referApp", "引用方应用"), ServiceName("serviceName", "服务名字"), Pid("pid", "进程号"), ReferHost(
              "referHost", "引用方节点"), ReferGroup("referGroup", "引用组"), Protocol("protocol", "协议");

  private static Map<String, String> configMaps = new HashMap<>();

  static {
    for (HamsterConfigEnum config : HamsterConfigEnum.values()) {
      configMaps.put(config.key, config.desc);
    }
  }

  public static Map<String, String> getConfigMaps() {
    return configMaps;
  }

  private HamsterConfigEnum(String key, String desc) {
    this.key = key;
    this.desc = desc;
  }

  public String getKey() {
    return key;
  }

  public String getDesc() {
    return desc;
  }

  private String key;
  private String desc;
}
