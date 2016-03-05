package com.seaky.hamster.core.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;

/**
 * 
 * 拦截器的注解
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceInterceptorAnnotation {
  /**
   * 
   * 拦截器的名字
   * 
   * @return String 拦截器的名字
   */
  String name();

  /**
   * 拦截器的作用的阶段
   * 
   * @return 拦截器作用的阶段列表
   */
  ProcessPhase[] phases();

  /**
   * 
   * object creator 的名字
   * 
   * @return String object creator 的名字
   */
  String creator() default "default";

}
