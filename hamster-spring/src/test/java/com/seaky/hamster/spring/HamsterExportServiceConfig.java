package com.seaky.hamster.spring;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HamsterExportServiceConfig {

  @Autowired
  private HamsterSpringSupport support;

  @PostConstruct
  public void export() {

    ClassExportConfig config = new ClassExportConfig();
    config.setApp("test");
    config.setBeanId("localmath");
    ServiceConfig sc = new ServiceConfig();
    sc.setInterceptors("testInput");
    config.addMethodConfig("add", sc);
    support.export(Math.class, config);
  }
}

