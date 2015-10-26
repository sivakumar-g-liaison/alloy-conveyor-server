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

/**
*
* @author OFS
*
*/
public class ServiceBrokerToMailboxQueueProcessor implements QueueProcessor {

	private static final Logger logger = LogManager.getLogger(ServiceBrokerToMailboxQueueProcessor.class);
	
	@Override
	public void processMessage(String message) {

		logger.info("Consumed WORKTICKET [" + message + "]");

		new MailBoxService().executeFileWriter(message);

		logger.info("Processed WORKTICKET [" + message + "]");
	}
}
