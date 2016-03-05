package com.seaky.hamster.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 作用在接口上表明该接口是可以扩展的
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {
  /**
   * 扩展的key
   * 
   * @return
   */
  String value();

  /**
   * 
   * 使用那个object creator
   * 
   * @return String object creator的名字
   */
  String creator() default "default";

}
