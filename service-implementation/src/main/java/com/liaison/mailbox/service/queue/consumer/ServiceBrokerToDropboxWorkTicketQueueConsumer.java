/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.consumer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class that consumes the workticket from ServiceBrokerToDropboxWorkTicketQueue.
 * 
 * @author OFS
 */
public class ServiceBrokerToDropboxWorkTicketQueueConsumer {

	private static int threadCount;
	private static final Logger logger = LogManager.getLogger(ServiceBrokerToDropboxWorkTicketQueueConsumer.class);
	private static ServiceBrokerToDropboxWorkTicketQueueConsumer qConsumerInstance = null;

	private ServiceBrokerToDropboxWorkTicketQueueConsumer() {
		// defeat instantiation.
	}

	private  LinkedBlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<Runnable>();
	private  MailBoxThreadPoolExecutor execSrvc = new MailBoxThreadPoolExecutor(threadCount, threadCount,60L, TimeUnit.MILLISECONDS,linkedBlockingQueue,true);

	public void invokeDropboxQueue(String requestJSON) throws InterruptedException {
		execSrvc.execute(new DropboxQueueInvoker(requestJSON));

	}

	public void printExecutorDiagonostics(){
		logger.info("************************************************************************************************");
		logger.info("Number of tasks currently queued up and waiting for invocation {}",linkedBlockingQueue.size() );
		logger.info("Number of threads in Executor that are active {}",execSrvc.getActiveCount() );
		logger.info("Number of tasks  queued in total - from the time beginning {} ",execSrvc.getTaskCount() );
		logger.info("Number of tasks  completed in total - from the time beginning {} ",execSrvc.getCompletedTaskCount() );
		logger.info("************************************************************************************************");
	}

	/**
	 * Get MailboxProcessorQueueConsumer Instance.
	 * @return MailboxProcessorQueueConsumer
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static ServiceBrokerToDropboxWorkTicketQueueConsumer getDropboxQueueConsumerInstance() throws Exception {

		threadCount = Integer.parseInt(MailBoxUtil.getEnvironmentProperties().getString("mailbox.dropboxQueue.queue.consumer.thread.count"));
		if (qConsumerInstance == null) {
			qConsumerInstance = new ServiceBrokerToDropboxWorkTicketQueueConsumer();
		}

		return qConsumerInstance;
	}
}
