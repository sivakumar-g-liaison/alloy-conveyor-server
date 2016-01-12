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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.messagebus.queueprocessor.QueueProcessor;
import com.liaison.mailbox.service.core.MailBoxService;
import com.liaison.mailbox.service.core.MailBoxService.QueueMessageType;
import com.liaison.mailbox.service.thread.pool.AsyncProcessThreadPool;

/**
*
* @author OFS
*
*/
public class ServiceBrokerToMailboxQueueProcessor implements QueueProcessor {

	private static final Logger logger = LogManager.getLogger(ServiceBrokerToMailboxQueueProcessor.class);
	
	@Override
	public void processMessage(String message) {
		AsyncProcessThreadPool.getExecutorService().submit(new MailBoxService(message, QueueMessageType.WORKTICKET));
	}
}
