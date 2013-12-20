package com.liaison.mailbox.service.core.processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MailBoxThreadPoolExecutor extends ThreadPoolExecutor{

	public MailBoxThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,boolean allowCoreThreadTimeOut) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		// TODO Auto-generated constructor stub
		this.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
	}

}

