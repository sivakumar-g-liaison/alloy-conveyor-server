/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.sla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.queue.consumer.ServiceBrokerToMailboxWorkTicketConsumer;

/**
 * @author OFS
 *
 */
public class WatchDogInvoker implements Runnable {

	private String request = null;

	private static final Logger logger = LogManager.getLogger(WatchDogInvoker.class);

	public WatchDogInvoker(String request){
		this.request=request;
	}

	public MailboxSLAWatchDogService getService(){
	    return new MailboxSLAWatchDogService();
	}


	@Override
	public void run() {

		logger.info("watchdog request:"+request+"handed over to thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());

		getService().invokeWatchDog(request);

		logger.info("watchdog request:"+request+" is completed by thread name:"+Thread.currentThread().getName()+" id:"+Thread.currentThread().getId());
		  try {
			   ServiceBrokerToMailboxWorkTicketConsumer.getMailboxWatchDogQueueConsumerInstance().printExecutorDiagonostics();
		  } catch(Exception e) {
				logger.error("Mailbox watchdog queue consumer thread count error", e);
		  }
	}

}
