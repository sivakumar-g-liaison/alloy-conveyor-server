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
 * Customized exception for mailbox service operations.
 * 
 * @author veerasamyn
 */
public class MailBoxServicesException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MailBoxServicesException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public MailBoxServicesException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	
	public MailBoxServicesException(Messages message) {
		super(message.value());
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param message
	 */
	public MailBoxServicesException(Messages message,String key) {
		super(String.format(message.value(),key));
		// TODO Auto-generated constructor stub
	}

}
