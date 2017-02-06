package com.seaky.hamster.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.server.ServerConfig;

@Configuration
@Component
public class HamsterConfig extends HamsterSpringSupport {


  private String urls = "http://localhost:2379";

  private String basepath = "hamster";



  @Override
  public void initRegisterationService() {

    createEtcdRegisterationService(getBasepath(), getUrls());
  }

  @Override
  public void initServer() {
    createServer("hamster", new ServerConfig());
  }

  @Override
  public void initClient() {
    createClient("hamster", new ClientConfig());

  }

  public String getUrls() {
    return urls;
  }

  public void setUrls(String urls) {
    this.urls = urls;
  }

  public String getBasepath() {
    return basepath;
  }

  public void setBasepath(String basepath) {
    this.basepath = basepath;
  }

}
