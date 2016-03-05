package com.seaky.hamster.core.rpc.executor;

import java.util.concurrent.ConcurrentHashMap;

import com.seaky.hamster.core.rpc.config.ConfigConstans;


public class ServiceThreadpoolManager {

	private  ConcurrentHashMap<String, ServiceThreadpool> allThreadpools = new ConcurrentHashMap<String, ServiceThreadpool>();

	private String prefix;
	
	private int commonPoolsize;
	
	private int commonPoolMaxQueue;
	public ServiceThreadpoolManager(String prefix,int commonPoolsize,int commonPoolMaxQueue)
	{
		this.prefix=prefix;
		this.commonPoolsize=commonPoolsize;
		this.commonPoolMaxQueue=commonPoolMaxQueue;
	}
	
	public  ServiceThreadpool create(String name, int maxThread,
			int maxQueue) {
		 name=prefix+name;
		ServiceThreadpool threadpool = allThreadpools.get(name);
		if (threadpool == null) {
			threadpool = new ServiceThreadpool(name, maxThread, maxQueue);
			ServiceThreadpool oldThreadpool = allThreadpools.putIfAbsent(name,
					threadpool);
			if (oldThreadpool != null) {
				threadpool = oldThreadpool;
			}
			threadpool.start();
		}
		if (threadpool.getMaxQueue() < maxQueue) {
			threadpool.setMaxQueue(maxQueue);
		}
		if (threadpool.getMaxThread() < maxThread) {
			threadpool.setMaxThread(maxThread);
		}
		return threadpool;
	}

	public  ServiceThreadpool createDefault() {
		return create(ConfigConstans.PROVIDER_THREADPOOL_NAME,
				commonPoolsize,
				commonPoolMaxQueue);
	}

	public  void stop() {
		for (ServiceThreadpool pool : allThreadpools.values()) {
			pool.stop();
		}
	}
}
