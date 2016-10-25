package com.seaky.hamster.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.seaky.hamster.admin.EtcdRegisterationManageService;

@Configuration
public class BeanConfig {

  @Bean
  public EtcdRegisterationManageService etcdRegisterationManageService() {

    return new EtcdRegisterationManageService("hamster",
        "http://192.168.20.171:2379,http://192.168.20.172:2379,http://192.168.20.173:2379");

  }
}
