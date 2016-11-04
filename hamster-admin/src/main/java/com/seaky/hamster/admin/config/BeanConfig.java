package com.seaky.hamster.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.seaky.hamster.admin.EtcdRegisterationManageService;

@Configuration
public class BeanConfig {

  @Value("${hamster.basePath}")
  private String basePah;

  @Value("${hamster.clusterAddrs}")
  private String clusterAddrs;

  @Bean
  public EtcdRegisterationManageService etcdRegisterationManageService() {

    return new EtcdRegisterationManageService(basePah, clusterAddrs);

  }

  public String getBasePah() {
    return basePah;
  }

  public void setBasePah(String basePah) {
    this.basePah = basePah;
  }

  public String getClusterAddrs() {
    return clusterAddrs;
  }

  public void setClusterAddrs(String clusterAddrs) {
    this.clusterAddrs = clusterAddrs;
  }
}
