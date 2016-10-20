/**
 * Copyright 2015 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.topic.receiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.messagebus.topic.TopicTextMessageProcessor;


public class MailBoxTopicMessageReceiver implements TopicTextMessageProcessor {

	private final Logger logger = LogManager.getLogger(MailBoxTopicMessageReceiver.class);

	/**
	 *  
	 *  Process the message. 
	 */
	@Override
	public void processMessage(String message) {

		if (message != null) {

			logger.debug("Message received from Topic");

			MessageHandlerFactory.getInstance().handle(message);
		}
	}
}
