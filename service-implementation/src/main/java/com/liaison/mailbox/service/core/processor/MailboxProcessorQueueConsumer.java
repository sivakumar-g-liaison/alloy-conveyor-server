/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailboxProcessorQueueConsumer {
	
	private final static int THREAD_COUNT = 5;	
	private static final Logger logger = LoggerFactory.getLogger(MailboxProcessorQueueConsumer.class);
	private static MailboxProcessorQueueConsumer qConsumerInstance = null;
	
	private MailboxProcessorQueueConsumer() {
		// defeat instantiation.
	}

	
	//ExecutorService execSrvc = Executors.newFixedThreadPool(THREAD_COUNT);
	//ExecutorService execSrvc = Executors.newCachedThreadPool();
	private LinkedBlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<Runnable>();
	private MailBoxThreadPoolExecutor execSrvc = new MailBoxThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,60L, TimeUnit.MILLISECONDS,linkedBlockingQueue,true);
	
	public void invokeProcessor(String requestJSON) throws InterruptedException {		
		execSrvc.execute(new ProcessorInvoker(requestJSON));
		
	}
 
	public void printExecutorDiagonostics(){
		logger.info("************************************************************************************************");
		logger.info("Number of tasks currently queued up and waiting for invocation {}",linkedBlockingQueue.size() );
		logger.info("Number of threads in Executor that are active {}",execSrvc.getActiveCount() );
		logger.info("Number of tasks  queued in total - from the time beginning {} ",execSrvc.getTaskCount() );
		logger.info("Number of tasks  completed in total - from the time beginning {} ",execSrvc.getCompletedTaskCount() );
		logger.info("************************************************************************************************");
	}
	
	public static MailboxProcessorQueueConsumer getMailboxProcessorQueueConsumerInstance() {
		if (qConsumerInstance == null) {
			qConsumerInstance = new MailboxProcessorQueueConsumer();
		}
		
		return qConsumerInstance;
	}
}