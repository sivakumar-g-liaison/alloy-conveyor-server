/**
 * Copyright 2015 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.topic.receiver;

/**
 * 
 * <code>MessageHandler</code> that handles the message received from topic processor.
 * 
 */
public interface MessageHandler {

	/**
	 *  
	 * Process the message.
	 *
	 * @param message - Actual message that needs to be processed.
	 */
	void handle(String message);
}
