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
import com.google.inject.Provider;
import com.liaison.mailbox.service.topic.KillThread;

/**
 * <code>MailBoxTopicMessageHandlerProvider<code> used to get the instance of MailBoxTopicMessageHandler. 
 * 
 */
public class MailBoxTopicMessageHandlerProvider implements Provider<MailBoxTopicMessageHandler> {

	private Provider<KillThread> killThreadProvider;

	/**
	 * Creates an new instance
	 * 
	 * @param channelSwitchProvider
	 */
	@Inject
    public MailBoxTopicMessageHandlerProvider(Provider<KillThread> killThreadProvider) {
        this.killThreadProvider = killThreadProvider;
    }

	/**
	 * Gets the MailBoxTopicMessageHandler 
	 */
	@Override 
	public MailBoxTopicMessageHandler get() {
		return new MailBoxTopicMessageHandler(killThreadProvider);
	}
}
