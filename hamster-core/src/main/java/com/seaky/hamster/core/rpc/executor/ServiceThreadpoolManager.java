package com.seaky.hamster.core.rpc.executor;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;


public class ServiceThreadpoolManager {

  private ConcurrentHashMap<String, EventExecutorGroup> allThreadpools =
      new ConcurrentHashMap<String, EventExecutorGroup>();

  private String prefix;


  public ServiceThreadpoolManager(String prefix) {
    this.prefix = prefix;
  }

  public EventExecutorGroup create(String name, int maxThread) {
    name = prefix + name;
    EventExecutorGroup threadpool = allThreadpools.get(name);
    if (threadpool == null) {
      threadpool =
          new DefaultEventExecutorGroup(maxThread, new NamedThreadFactory("servicepool-" + name));
      EventExecutorGroup oldThreadpool = allThreadpools.putIfAbsent(name, threadpool);
      if (oldThreadpool != null) {
        threadpool.shutdownGracefully();
        threadpool = oldThreadpool;
      }
    }
    if (maxThread > threadpool.children().size()) {
      EventExecutorGroup newThreadpool =
          new DefaultEventExecutorGroup(maxThread, new NamedThreadFactory("servicepool-" + name));
      allThreadpools.put(name, newThreadpool);
      threadpool.shutdownGracefully();
      return newThreadpool;
    }
    return threadpool;
  }


  public void stop() {
    for (EventExecutorGroup pool : allThreadpools.values()) {
      pool.shutdownGracefully();
    }
  }
}
