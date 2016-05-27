package com.seaky.hamster.core.rpc.client;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.executor.NamedThreadFactory;
import com.seaky.hamster.core.rpc.executor.ServiceThreadpoolManager;
import com.seaky.hamster.core.rpc.utils.Utils;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class ClientResourceManager {

  private static boolean isStart;

  private static EventLoopGroup ioGroup;

  private static HashedWheelTimer wheelTimer = null;

  private static ServiceThreadpoolManager serviceThreadpoolManager;

  private static EventExecutorGroup asynExecutorPool;

  private static Logger logger = LoggerFactory.getLogger(ClientResourceManager.class);

  public synchronized static void start() {
    if (!isStart) {
      ioGroup = new NioEventLoopGroup(ClientGlobalConfig.getIoThreadNum(),
          new NamedThreadFactory("hamster-client-io-pool-worker"));
      logger.info("client io pool start,pool size is {}", ClientGlobalConfig.getIoThreadNum());
      wheelTimer = new HashedWheelTimer(new NamedThreadFactory("hamster-client-wheel-timer"), 500,
          TimeUnit.MILLISECONDS, 1024 * 10);
      logger.info("client wheel timer thread start,tick is {}{},wheel size is {}", 500,
          TimeUnit.MILLISECONDS.name(), 1024 * 10);

      serviceThreadpoolManager = new ServiceThreadpoolManager("client_");

      asynExecutorPool =
          new DefaultEventExecutorGroup(ClientGlobalConfig.getAsynExecutorThreadNum(),
              new NamedThreadFactory("hamster-client-dispatcher-pool"));
      logger.info("client asyn executor start,size is {}",
          ClientGlobalConfig.getAsynExecutorThreadNum());
      isStart = true;
    }
  }

  public static EventExecutorGroup getAsynExecutorPool() {
    return asynExecutorPool;
  }

  /**
   * @return the ioGroup
   */
  public static EventLoopGroup getIoGroup() {
    return ioGroup;
  }

  public synchronized static void stop() {
    if (isStart) {
      if (ioGroup != null) {
        try {
          ioGroup.shutdownGracefully();
        } catch (Exception e) {

          logger.error("shutdown client io pool error", e);
        }
        ioGroup = null;
      }
      if (wheelTimer != null) {
        try {
          wheelTimer.stop();
        } catch (Exception e) {
          logger.error("shutdown client wheel timer error", e);
        }
        wheelTimer = null;
      }
      if (serviceThreadpoolManager != null) {
        serviceThreadpoolManager.stop();
      }
      if (asynExecutorPool != null) {
        try {
          Utils.shutdownExecutorService(asynExecutorPool, 30);
        } catch (Exception e) {
          logger.error("shutdown client asyn executor  error", e);
        }
        asynExecutorPool = null;
      }
      isStart = false;
    }
  }

  /**
   * @return the referUpdateExecutor
   */

  public static HashedWheelTimer getHashedWheelTimer() {
    return wheelTimer;
  }

  /**
   * @return the serviceThreadpoolManager
   */
  public static ServiceThreadpoolManager getServiceThreadpoolManager() {
    return serviceThreadpoolManager;
  }

}
