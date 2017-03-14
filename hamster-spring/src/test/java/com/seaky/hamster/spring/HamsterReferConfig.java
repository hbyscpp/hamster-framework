package com.seaky.hamster.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class HamsterReferConfig {

  @Autowired
  private HamsterSpringSupport support;



  @Bean(name = "remotemath")
  public Math math() {
    ClassReferenceConfig config = new ClassReferenceConfig();
    config.setApp("test");
    ReferenceConfig cf = new ReferenceConfig();
    cf.setReadtimeout(100 * 1000);
    config.addMethodConfig("delay", cf);
    return support.referenceService(Math.class, config);
  }
}

