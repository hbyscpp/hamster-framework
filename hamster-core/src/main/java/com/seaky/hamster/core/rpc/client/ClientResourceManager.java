package com.seaky.hamster.core.rpc.client;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.executor.NamedThreadFactory;
import com.seaky.hamster.core.rpc.executor.ServiceThreadpoolManager;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.internal.chmv8.ForkJoinPool;
import io.netty.util.internal.chmv8.ForkJoinPool.ForkJoinWorkerThreadFactory;

public class ClientResourceManager {

	private static boolean isStart;

	private static EventLoopGroup ioGroup;

	private static HashedWheelTimer wheelTimer = null;

	private static ServiceThreadpoolManager serviceThreadpoolManager;

	private static ForkJoinPool forkjoinpool;

	private static Logger logger = LoggerFactory
			.getLogger(ClientResourceManager.class);

	public synchronized static void start() {
		if (!isStart) {
			ioGroup = new NioEventLoopGroup(
					ClientGlobalConfig.getIoThreadNum(),
					new NamedThreadFactory("hamster-client-io-pool-worker"));
			logger.info("client io pool start,pool size is {}",
					ClientGlobalConfig.getIoThreadNum());
			wheelTimer = new HashedWheelTimer(new NamedThreadFactory(
					"hamster-client-wheel-timer"), 500, TimeUnit.MILLISECONDS,
					1024 * 10);
			logger.info(
					"client wheel timer thread start,tick is {}{},wheel size is {}",
					500, TimeUnit.MILLISECONDS.name(), 1024 * 10);

			serviceThreadpoolManager = new ServiceThreadpoolManager("client_",
					ClientGlobalConfig.getDefaultPoolThreadNum(),
					ClientGlobalConfig.getDefaultPoolMaxQueue());

			try {
				Constructor<ForkJoinPool> poolConstructor = ForkJoinPool.class
						.getDeclaredConstructor(int.class,
								ForkJoinWorkerThreadFactory.class,
								UncaughtExceptionHandler.class, int.class,
								String.class);
				poolConstructor.setAccessible(true);
				forkjoinpool = poolConstructor.newInstance(
						ClientGlobalConfig.getAsynExecutorThreadNum(),
						ForkJoinPool.defaultForkJoinWorkerThreadFactory, null,
						1, "hamster-client-asyn-executor-worker");
				logger.info("client asyn executor start,size is {}",
						ClientGlobalConfig.getAsynExecutorThreadNum());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			isStart = true;
		}
	}

	public static ForkJoinPool getAsynExecutorPool() {
		return forkjoinpool;
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
			if (forkjoinpool != null) {
				try {
					forkjoinpool.shutdown();
				} catch (Exception e) {
					logger.error("shutdown client asyn executor  error", e);

				}
				forkjoinpool = null;
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
