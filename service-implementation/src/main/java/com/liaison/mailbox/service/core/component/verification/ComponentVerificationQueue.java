/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.component.verification;

import com.google.inject.Singleton;
import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreSendReceiveClient;

/**
 * @author OFS.
 * 
 * Dummy Queue to send/receive message for checking the configuration
 */
@Singleton
public class ComponentVerificationQueue extends HornetQRecoveringCoreSendReceiveClient {

	public static final String QUEUE_NAME = "componentVerificationQueue";
	private static ComponentVerificationQueue ourInstance = new ComponentVerificationQueue();

	public static ComponentVerificationQueue getInstance() {
		return ourInstance;
	}

	private ComponentVerificationQueue() {
		super(QUEUE_NAME);
	}

}
