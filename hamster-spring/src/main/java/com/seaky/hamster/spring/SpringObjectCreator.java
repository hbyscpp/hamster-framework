package com.seaky.hamster.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.seaky.hamster.core.objectfactory.ObjectCreator;

public class SpringObjectCreator implements ObjectCreator {

  private ApplicationContext context;


  @Override
  public <T> T create(Class<T> cls) {
    return context.getBean(cls);
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

}
