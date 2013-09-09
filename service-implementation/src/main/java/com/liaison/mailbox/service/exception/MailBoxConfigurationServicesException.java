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

/**
 * Customized exception for mailbox operations.
 * 
 * @author veerasamyn
 */
public class MailBoxConfigurationServicesException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MailBoxConfigurationServicesException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public MailBoxConfigurationServicesException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
