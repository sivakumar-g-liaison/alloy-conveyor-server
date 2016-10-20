/**
 * Copyright 2015 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.topic.receiver;

import com.google.inject.Inject;

/**
 * <code>MessageFactory<code> is a factory class that returns the MessageHandler
 * that would be used after receiving the message from topic.
 * 
 */
public class MessageHandlerFactory {

	@Inject
	private static MailBoxTopicMessageHandlerProvider messageHandlerProvider;

	/**
	 * Gets the instance of MailBoxHandler.
	 */
	public static MessageHandler getInstance() {
		return messageHandlerProvider.get();
	}
}
