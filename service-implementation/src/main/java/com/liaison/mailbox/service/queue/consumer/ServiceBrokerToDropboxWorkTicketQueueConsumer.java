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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.threadmanagement.LiaisonExecutorServiceBuilder;

public class ServiceBrokerToDropboxWorkTicketQueueConsumer {

	private static int threadCount;
	private static final Logger logger = LogManager.getLogger(ServiceBrokerToDropboxWorkTicketQueueConsumer.class);
	private static ServiceBrokerToDropboxWorkTicketQueueConsumer qConsumerInstance = null;

	private ServiceBrokerToDropboxWorkTicketQueueConsumer() {
		// defeat instantiation.
	}

	private ExecutorService execSrvc = LiaisonExecutorServiceBuilder.newExecutorService("ServiceBrokerToDropboxQueueExecutorPool", threadCount, threadCount, 60, TimeUnit.MILLISECONDS);
	public void invokeDropboxQueue(String requestJSON) throws InterruptedException {
		execSrvc.execute(new DropboxQueueInvoker(requestJSON));

	}

	public void printExecutorDiagonostics(){

		ThreadPoolExecutor executor = (ThreadPoolExecutor) execSrvc;

		logger.info("************************************************************************************************");
		logger.info("Number of tasks currently queued up and waiting for invocation {}", executor.getQueue().size() );
		logger.info("Number of threads in Executor that are active {}", executor.getActiveCount() );
		logger.info("Number of tasks  queued in total - from the time beginning {} ", executor.getTaskCount() );
		logger.info("Number of tasks  completed in total - from the time beginning {} ", executor.getCompletedTaskCount() );
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
