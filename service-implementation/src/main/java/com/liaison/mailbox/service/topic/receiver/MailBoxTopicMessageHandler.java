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

import com.google.inject.Provider;
import com.liaison.mailbox.service.topic.KillThread;

/**
 * <code>MailBoxTopicMessageHandler<code> that handles messages to kill threads. 
 * 
 */
public class MailBoxTopicMessageHandler implements MessageHandler {

	private final Logger logger = LogManager.getLogger(MailBoxTopicMessageHandler.class);

	private Provider<KillThread> killThreadProvider;

	public MailBoxTopicMessageHandler(Provider<KillThread> channelSwitchProvider) {
		this.killThreadProvider = channelSwitchProvider;
	}

	/**
	 *  
	 *  Process the message. 
	 */
	public void handle(String node) {

		logger.debug("Message received [ {} ]", node );

		// return the node is current node
		/*if (ServerIdentifier.INSTANCE.compareTo(message)) {
			return; // Represents that the message received is actually posted from this server. 
		}*/

		killThreadProvider.get().equals(node);
	}
}
