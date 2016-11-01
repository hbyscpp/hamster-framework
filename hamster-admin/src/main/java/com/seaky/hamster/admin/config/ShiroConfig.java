package com.seaky.hamster.admin.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {
  private static Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

  @Bean(name = "ShiroRealmImpl")
  public Realm getShiroRealm() {
    return new IniRealm("classpath:user.ini");
  }

  @Bean(name = "shiroEhcacheManager")
  public CacheManager getEhCacheManager() {
    MemoryConstrainedCacheManager em = new MemoryConstrainedCacheManager();
    return em;
  }

  @Bean(name = "lifecycleBeanPostProcessor")
  public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
    return new LifecycleBeanPostProcessor();
  }


  @Bean(name = "securityManager")
  public DefaultWebSecurityManager getDefaultWebSecurityManager() {
    DefaultWebSecurityManager dwsm = new DefaultWebSecurityManager();
    dwsm.setRealm(getShiroRealm());
    dwsm.setCacheManager(getEhCacheManager());
    return dwsm;
  }

  @Bean
  public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
    AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
    aasa.setSecurityManager(getDefaultWebSecurityManager());
    return aasa;
  }

  @Bean(name = "shiroFilter")
  public ShiroFilterFactoryBean getShiroFilterFactoryBean() {
    ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
    shiroFilterFactoryBean.setSecurityManager(getDefaultWebSecurityManager());
    shiroFilterFactoryBean.setLoginUrl("index.html#/login");
    shiroFilterFactoryBean.setSuccessUrl("/index");
    filterChainDefinitionMap.put("/checkpwd", "anon");
    filterChainDefinitionMap.put("/**/*.html", "anon");
    filterChainDefinitionMap.put("/**/*.css", "anon");
    filterChainDefinitionMap.put("/**/*.js", "anon");
    filterChainDefinitionMap.put("/css/**", "anon");
    filterChainDefinitionMap.put("/js/**", "anon");
    filterChainDefinitionMap.put("/img/*.js", "anon");
    filterChainDefinitionMap.put("/**", "authc");
    shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
    return shiroFilterFactoryBean;
  }
}
