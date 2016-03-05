package com.seaky.hamster.core.rpc.server;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.executor.NamedThreadFactory;
import com.seaky.hamster.core.rpc.executor.ServiceThreadpoolManager;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.chmv8.ForkJoinPool;
import io.netty.util.internal.chmv8.ForkJoinPool.ForkJoinWorkerThreadFactory;

//全局资源的管理
public class ServerResourceManager {

	private static EventLoopGroup bossGroup = null;

	private static EventLoopGroup workerGroup = null;

	private static ForkJoinPool forkjoinpool;

	private static boolean isStart = false;

	private static ServiceThreadpoolManager serviceThreadpoolManager;

	static ForkJoinPool getDispatcherPool() {
		return forkjoinpool;
	}

	static EventLoopGroup getBossGroup() {
		return bossGroup;
	}

	static EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	private static Logger logger = LoggerFactory
			.getLogger(ServerResourceManager.class);

	public static synchronized void start() {
		if (isStart)
			return;
		try {
			Constructor<ForkJoinPool> poolConstructor = ForkJoinPool.class
					.getDeclaredConstructor(int.class,
							ForkJoinWorkerThreadFactory.class,
							UncaughtExceptionHandler.class, int.class,
							String.class);
			poolConstructor.setAccessible(true);
			forkjoinpool=poolConstructor.newInstance(
					ServerGlobalConfig.getDispatcherThreadNum(),
					ForkJoinPool.defaultForkJoinWorkerThreadFactory, null,
					1, "hamster-server-dispatcher-pool-worker");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		logger.info("dispatcher pool start, pool size is {}",
				ServerGlobalConfig.getDispatcherThreadNum());
		bossGroup = new NioEventLoopGroup(
				ServerGlobalConfig.getAcceptorThreadNum(),
				new NamedThreadFactory("hamster-server-connect-pool-worker"));
		logger.info("server network connect pool start,pool size is {}",
				ServerGlobalConfig.getAcceptorThreadNum());
		workerGroup = new NioEventLoopGroup(
				ServerGlobalConfig.getIoThreadNum(), new NamedThreadFactory(
						"hamster-server-io-pool-worker"));
		logger.info("server io pool start,pool size is {}",
				ServerGlobalConfig.getIoThreadNum());
		serviceThreadpoolManager = new ServiceThreadpoolManager("server_",
				ServerGlobalConfig.getDefaultThreadpoolNum(),
				ServerGlobalConfig.getDefaultMaxQueue());
		isStart = true;

	}

	// 释放所有服务端公共资源
	public static synchronized void stop() {
		if (isStart) {
			try {
				if (bossGroup != null)
					bossGroup.shutdownGracefully();
			} catch (Exception e) {
				logger.error("shutdown server connect pool error.", e);

			}
			bossGroup = null;
			try {
				if (workerGroup != null)
					workerGroup.shutdownGracefully();
			} catch (Exception e) {
				logger.error("shutdown server io pool error.", e);
			}
			workerGroup = null;
			try {
				if (forkjoinpool != null) {
					forkjoinpool.shutdown();
					forkjoinpool.awaitTermination(30, TimeUnit.SECONDS);
				}
			} catch (Exception e) {
				logger.error("shutdown server dispatcher pool error", e);
			}
			forkjoinpool = null;

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
