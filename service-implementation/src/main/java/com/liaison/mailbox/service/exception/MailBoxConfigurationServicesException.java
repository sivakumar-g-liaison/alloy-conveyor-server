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
 * Customized exception for mailbox configuration service operations.
 *
 * @author veerasamyn
 */
public class MailBoxConfigurationServicesException extends LiaisonAuditableRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
     * @param message
     */
	public MailBoxConfigurationServicesException(Messages message, Response.Status status) {
		super(new Exception(message.value()), status);
	}

    /**
     * @param message
     */
	public MailBoxConfigurationServicesException(Messages message, String tokenReplacementKey, Response.Status status) {
		super(new Exception(String.format(message.value(), tokenReplacementKey)), status);
	}


    /**
     * @param message
     */
    public MailBoxConfigurationServicesException(String message, Response.Status status) {
        super(new Exception(message),status);
    }

    /**
     * @param message
     */
	public MailBoxConfigurationServicesException(Messages message, String tokenReplacementKey, Response.Status status,
			String additionalMessage) {
		super(new Exception(String.format(message.value(), tokenReplacementKey) + additionalMessage), status);
	}

}
