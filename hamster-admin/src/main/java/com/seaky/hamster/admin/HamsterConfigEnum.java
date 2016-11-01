package com.seaky.hamster.admin;

public enum HamsterConfigEnum {

  App("app", "应用"), Version("version", "版本"), Params("paramTypes", "参数类型"), Return("returnType",
      "返回类型"), Host("host", "节点"), Port("port", "端口"), RegTime("registTime", "注册时间");

  private HamsterConfigEnum(String key, String desc) {

  }

}
