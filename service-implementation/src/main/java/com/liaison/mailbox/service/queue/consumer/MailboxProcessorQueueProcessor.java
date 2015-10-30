/**
 * Copyright 2015 Liaison Technologies, Inc. All rights reserved.
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
 * @author Ghazni Nattarshah
 *
 */
public class MailboxProcessorQueueProcessor implements QueueProcessor {

	private static final Logger logger = LogManager.getLogger(MailboxProcessorQueueProcessor.class);

	@Override
	public void processMessage(String message) {

		logger.info("Consumed Trigger profile request [" + message + "]");
		
		new MailBoxService().executeProcessor(message);
		
		logger.info("Processor processed Trigger profile request [" + message + "]");
	}
}
