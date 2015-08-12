/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.exception;

import com.liaison.mailbox.enums.Messages;

/**
 * Customized exception for mailbox configuration service operations.
 * 
 * @author veerasamyn
 */
public class MailBoxFSMSetupException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MailBoxFSMSetupException() {
	}

	/**
	 * @param message
	 */
	public MailBoxFSMSetupException(Messages message, String key) {
		super(String.format(message.value(), key));
	}

	/**
	 * @param message
	 */
	public MailBoxFSMSetupException(Messages message) {
		super(message.value());
	}

	/**
	 * @param message
	 */
	public MailBoxFSMSetupException(String message) {
		super(message);
	}

}
