package com.seaky.hamster.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class App {

  /**
   * @param args
   */
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
