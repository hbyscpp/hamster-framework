package com.seaky.hamster.core.rpc.client;

public class ClientGlobalConfig {

	//网络io读写线程数目
	private static int ioThreadNum=Runtime.getRuntime().availableProcessors()*2;

	private static int defaultPoolThreadNum=Runtime.getRuntime().availableProcessors();
	
	private static int defaultPoolMaxQueue=10000000;
	
	
	private static int asynExecutorThreadNum = Runtime.getRuntime()
			.availableProcessors();
	/**
	 * @return the ioThreadNum
	 */
	public static int getIoThreadNum() {
		return ioThreadNum;
	}

	/**
	 * @param ioThreadNum the ioThreadNum to set
	 */
	public static void setIoThreadNum(int ioThreadNum) {
		ClientGlobalConfig.ioThreadNum = ioThreadNum;
	}

	/**
	 * @return the defaultPoolThreadNum
	 */
	public static int getDefaultPoolThreadNum() {
		return defaultPoolThreadNum;
	}

	/**
	 * @param defaultPoolThreadNum the defaultPoolThreadNum to set
	 */
	public static void setDefaultPoolThreadNum(int defaultPoolThreadNum) {
		ClientGlobalConfig.defaultPoolThreadNum = defaultPoolThreadNum;
	}

	/**
	 * @return the defaultPoolMaxQueue
	 */
	public static int getDefaultPoolMaxQueue() {
		return defaultPoolMaxQueue;
	}

	/**
	 * @param defaultPoolMaxQueue the defaultPoolMaxQueue to set
	 */
	public static void setDefaultPoolMaxQueue(int defaultPoolMaxQueue) {
		ClientGlobalConfig.defaultPoolMaxQueue = defaultPoolMaxQueue;
	}


	/**
	 * @return the asynExecutorThreadNum
	 */
	public static int getAsynExecutorThreadNum() {
		return asynExecutorThreadNum;
	}

	/**
	 * @param asynExecutorThreadNum the asynExecutorThreadNum to set
	 */
	public static void setAsynExecutorThreadNum(int asynExecutorThreadNum) {
		ClientGlobalConfig.asynExecutorThreadNum = asynExecutorThreadNum;
	}
}
