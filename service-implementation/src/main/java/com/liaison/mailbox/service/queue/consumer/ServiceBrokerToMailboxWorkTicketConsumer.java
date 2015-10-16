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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.core.sla.WatchDogInvoker;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.threadmanagement.LiaisonExecutorServiceBuilder;

/**
*
* @author OFS
*
*/
public class ServiceBrokerToMailboxWorkTicketConsumer {

	private static int threadCount;
	private static final Logger logger = LogManager.getLogger(ServiceBrokerToMailboxWorkTicketConsumer.class);
	private static ServiceBrokerToMailboxWorkTicketConsumer qConsumerInstance = null;

	private ServiceBrokerToMailboxWorkTicketConsumer() {
		// defeat instantiation.
	}

	private ExecutorService execSrvc = LiaisonExecutorServiceBuilder.newScheduledExecutorService("g2-pool-servicebroker-to-mailbox-consumer", threadCount);

	public void invokeWatchDog(String requestJSON) throws InterruptedException {
		execSrvc.execute(new WatchDogInvoker(requestJSON));

	}

	public void printExecutorDiagonostics(){

		if (execSrvc == null) {
			return;
		}

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
	public static ServiceBrokerToMailboxWorkTicketConsumer getMailboxWatchDogQueueConsumerInstance() throws Exception {

		threadCount = Integer.parseInt(MailBoxUtil.getEnvironmentProperties().getString("mailbox.processedPayload.queue.consumer.thread.count"));
		if (qConsumerInstance == null) {
			qConsumerInstance = new ServiceBrokerToMailboxWorkTicketConsumer();
		}

		return qConsumerInstance;
	}
}
