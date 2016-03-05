package com.seaky.hamster.core.rpc.server;

//服务端的全局配置
public class ServerGlobalConfig {

	//处理连接的线程数目
	private static int acceptorThreadNum = Runtime.getRuntime()
			.availableProcessors();

	//网络IO的线程数目
	private static int ioThreadNum = 2 * Runtime.getRuntime()
			.availableProcessors();

	//业务分发的线程数目
	private static int dispatcherThreadNum = Runtime.getRuntime()
			.availableProcessors();

	//默认公共的线程池线程数目
	private static int defaultThreadpoolNum = 200;

	//默认线程池最大队列
	private static int defaultMaxQueue = 1000000;

	/**
	 * @return the acceptorThreadNum
	 */
	public static int getAcceptorThreadNum() {
		return acceptorThreadNum;
	}

	/**
	 * @param acceptorThreadNum
	 *            the acceptorThreadNum to set
	 */
	public static void setAcceptorThreadNum(int acceptorThreadNum) {
		ServerGlobalConfig.acceptorThreadNum = acceptorThreadNum;
	}

	/**
	 * @return the ioThreadNum
	 */
	public static int getIoThreadNum() {
		return ioThreadNum;
	}

	/**
	 * @param ioThreadNum
	 *            the ioThreadNum to set
	 */
	public static void setIoThreadNum(int ioThreadNum) {
		ServerGlobalConfig.ioThreadNum = ioThreadNum;
	}

	/**
	 * @return the dispatcherThreadNum
	 */
	public static int getDispatcherThreadNum() {
		return dispatcherThreadNum;
	}

	/**
	 * @param dispatcherThreadNum
	 *            the dispatcherThreadNum to set
	 */
	public static void setDispatcherThreadNum(int dispatcherThreadNum) {
		ServerGlobalConfig.dispatcherThreadNum = dispatcherThreadNum;
	}

	/**
	 * @return the defaultThreadpoolNum
	 */
	public static int getDefaultThreadpoolNum() {
		return defaultThreadpoolNum;
	}

	/**
	 * @param defaultThreadpoolNum the defaultThreadpoolNum to set
	 */
	public static void setDefaultThreadpoolNum(int defaultThreadpoolNum) {
		ServerGlobalConfig.defaultThreadpoolNum = defaultThreadpoolNum;
	}

	/**
	 * @return the defaultMaxQueue
	 */
	public static int getDefaultMaxQueue() {
		return defaultMaxQueue;
	}

	/**
	 * @param defaultMaxQueue the defaultMaxQueue to set
	 */
	public static void setDefaultMaxQueue(int defaultMaxQueue) {
		ServerGlobalConfig.defaultMaxQueue = defaultMaxQueue;
	}
}
