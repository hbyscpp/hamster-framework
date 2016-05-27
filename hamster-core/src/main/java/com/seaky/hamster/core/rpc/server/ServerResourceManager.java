package com.seaky.hamster.core.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.executor.NamedThreadFactory;
import com.seaky.hamster.core.rpc.executor.ServiceThreadpoolManager;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

// 全局资源的管理
public class ServerResourceManager {

  // 接受连接的线程池
  private static EventLoopGroup connectThreadPool = null;
  // 读写线程池
  private static EventLoopGroup ioThreadPool = null;
  // 协议解析分发线程
  private static EventLoopGroup dispatcherThreadPool = null;

  private static boolean isStart = false;

  private static ServiceThreadpoolManager serviceThreadpoolManager;

  public static EventLoopGroup getDispatcherThreadPool() {
    return dispatcherThreadPool;
  }

  static EventLoopGroup getConnectThreadPool() {
    return connectThreadPool;
  }

  static EventLoopGroup getIOThreadPool() {
    return ioThreadPool;
  }

  private static Logger logger = LoggerFactory.getLogger(ServerResourceManager.class);

  public static synchronized void start() {
    if (isStart)
      return;

    connectThreadPool = new NioEventLoopGroup(ServerGlobalConfig.getAcceptorThreadNum(),
        new NamedThreadFactory("hamster-server-connect-pool"));
    logger.info("server network connect pool start,pool size is {}",
        ServerGlobalConfig.getAcceptorThreadNum());
    ioThreadPool = new NioEventLoopGroup(ServerGlobalConfig.getIoThreadNum(),
        new NamedThreadFactory("hamster-server-io-pool"));
    logger.info("server io pool start,pool size is {}", ServerGlobalConfig.getIoThreadNum());
    dispatcherThreadPool = new DefaultEventLoopGroup(ServerGlobalConfig.getDispatcherThreadNum(),
        new NamedThreadFactory("hamster-server-dispatcher-pool"));
    logger.info("server dispatcher pool start, pool size is {}",
        ServerGlobalConfig.getDispatcherThreadNum());
    serviceThreadpoolManager = new ServiceThreadpoolManager("server_");
    isStart = true;

  }

  // 释放所有服务端公共资源
  public static synchronized void stop() {
    if (isStart) {
      try {
        if (connectThreadPool != null)
          connectThreadPool.shutdownGracefully();
      } catch (Exception e) {
        logger.error("shutdown server connect pool error.", e);

      }
      connectThreadPool = null;
      try {
        if (ioThreadPool != null)
          ioThreadPool.shutdownGracefully();
      } catch (Exception e) {
        logger.error("shutdown server io pool error.", e);
      }
      ioThreadPool = null;
      try {
        if (dispatcherThreadPool != null) {
          dispatcherThreadPool.shutdownGracefully();
        }
      } catch (Exception e) {
        logger.error("shutdown server dispatcher pool error.", e);
      }
      dispatcherThreadPool = null;

      if (serviceThreadpoolManager != null) {
        serviceThreadpoolManager.stop();
        serviceThreadpoolManager = null;
      }
      isStart = false;
    }

  }

  public static ServiceThreadpoolManager getServiceThreadpoolManager() {
    return serviceThreadpoolManager;
  }

}
