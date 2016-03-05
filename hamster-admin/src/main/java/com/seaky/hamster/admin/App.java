package com.seaky.hamster.admin;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration
@ComponentScan
@Configuration
public class App {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	public FilterRegistrationBean myFilter() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
	    registration.setFilter(new HeaderFilter());
	    registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
	    return registration;
	}

}
