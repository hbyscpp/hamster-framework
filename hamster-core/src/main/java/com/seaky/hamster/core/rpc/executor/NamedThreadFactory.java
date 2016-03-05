package com.seaky.hamster.core.rpc.executor;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

public class NamedThreadFactory implements ThreadFactory {

	private static final AtomicInteger threadPoolNumber = new AtomicInteger(1);
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private static final String NAME_PATTERN = "%s-%d-thread";
	private final String threadNamePrefix;

	private boolean isDeamon;

	public NamedThreadFactory(String threadNamePrefix) {
		this(threadNamePrefix, false);
	}

	/**
	 * Creates a new {@link NamedThreadFactory} instance
	 * 
	 * @param threadNamePrefix
	 *            the name prefix assigned to each thread created.
	 */
	public NamedThreadFactory(String threadNamePrefix, boolean isDeamon) {
		if(StringUtils.isBlank(threadNamePrefix))
			throw new RuntimeException("thread prefix can not be null");
		final SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
				.getThreadGroup();
		this.isDeamon = isDeamon;
		this.threadNamePrefix = String.format(Locale.ROOT, NAME_PATTERN,
				threadNamePrefix, threadPoolNumber.getAndIncrement());
	}

	/**
	 * Creates a new {@link Thread}
	 * 
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		final Thread t = new Thread(group, r,
				String.format(Locale.ROOT, "%s-%d", this.threadNamePrefix,
						threadNumber.getAndIncrement()), 0);
		t.setDaemon(isDeamon);
		t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

}
