/**
 * Copyright 2015 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.topic.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.messagebus.topic.TopicTextSendClient;

/**
 * 
 * Sends the messages to configured topic 
 */
public class MailBoxTopicSenderPool extends TopicTextSendClient {

	private static final Logger logger = LogManager.getLogger(MailBoxTopicSenderPool.class);

	private static final String POOL_TYPE = "topicpool";

	/**
	 * Creates an instance
	 */
	public MailBoxTopicSenderPool() {

		super(POOL_TYPE);
		logger.debug("Instantiating " + MailBoxTopicSenderPool.class.getName());
	}
}
