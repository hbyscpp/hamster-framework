package com.seaky.hamster.core.rpc.common;

import java.util.concurrent.ConcurrentHashMap;

import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 6, 2016
 */
public class DefaultServiceContext implements ServiceContext {


  private ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

  private ProcessPhase processPhase;


  public DefaultServiceContext(ProcessPhase processPhase) {
    this.processPhase = processPhase;
  }

  @Override
  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  @Override
  public void setAttribute(String key, Object obj) {
    if (obj == null)
      return;
    attributes.put(key, obj);
  }

  @Override
  public void removeAttribute(String key) {
    attributes.remove(key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAttribute(String key, Class<T> cls) {
    Object obj = getAttribute(key);
    if (obj == null)
      return null;
    return (T) obj;
  }

  @Override
  public ProcessPhase processPhase() {
    return processPhase;
  }


}
