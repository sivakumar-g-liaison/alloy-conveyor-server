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

import javax.ws.rs.core.Response;

import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.mailbox.enums.Messages;

/**
 * Customized exception for mailbox service operations.
 * 
 * @author veerasamyn
 */
public class MailBoxServicesException extends LiaisonAuditableRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	
	/**
	 * @param message
	 */
	public MailBoxServicesException(String message, Response.Status status) {
		super(new Exception(message), status);
	}
	
	
	public MailBoxServicesException(Messages message, Response.Status status) {
		super(new Exception(message.value()), status);
	}
	
	/**
	 * @param message
	 */
	public MailBoxServicesException(Messages message,String key, Response.Status status) {
		super(new Exception(String.format(message.value(), key)), status);
	}

}
