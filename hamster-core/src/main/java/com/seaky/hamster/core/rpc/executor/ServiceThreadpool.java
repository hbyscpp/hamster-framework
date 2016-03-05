package com.seaky.hamster.core.rpc.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ServiceThreadpool implements Executor{

	private String name;

	private ThreadPoolExecutor threadpool;

	private int maxThread = 10;

	private int maxQueue = 20;

	private BlockingQueue<Runnable> queue = null;

	private boolean isStart;

	public ServiceThreadpool(String name, int maxThread, int maxQueue) {
		this.name = name;
		this.maxThread=maxThread;
		this.maxQueue=maxQueue;

	}

	public synchronized void start() {
		if (isStart)
			return;
		queue = new LinkedBlockingQueue<Runnable>();
		threadpool = new ThreadPoolExecutor(maxThread, maxThread, 1800L,
				TimeUnit.SECONDS, queue, new NamedThreadFactory("servicepool-"
						+ name));
		isStart = true;
	}

	public synchronized void stop() {

		if (!isStart)
			return;
		if (threadpool != null) {
			//TODO shutdown 
			threadpool.shutdown();
		}
		isStart = false;

	}

	private void checkQueueSize() {
		if (queue.size() >= maxQueue)
			throw new RejectedExecutionException(name + " executor service has reach max queue "
					+ maxQueue);
	}

	public void execute(Runnable command) {
		checkQueueSize();
		threadpool.execute(command);
	}

	public Future<?> submit(Runnable command) {
		checkQueueSize();
		return threadpool.submit(command);
	}

	/**
	 * @return the maxThread
	 */
	public int getMaxThread() {
		return maxThread;
	}

	/**
	 * @param maxThread
	 *            the maxThread to set
	 */
	public void setMaxThread(int maxThread) {
		if (maxThread <= 0)
			throw new RuntimeException("maxThread must greater than zero");

		this.maxThread = maxThread;
		this.threadpool.setMaximumPoolSize(maxThread);
	}

	/**
	 * @return the maxQueue
	 */
	public int getMaxQueue() {
		return maxQueue;
	}

	/**
	 * @param maxQueue
	 *            the maxQueue to set
	 */
	public void setMaxQueue(int maxQueue) {
		if (maxQueue <= 0)
			throw new RuntimeException("maxQueue must greater than zero");
		this.maxQueue = maxQueue;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
