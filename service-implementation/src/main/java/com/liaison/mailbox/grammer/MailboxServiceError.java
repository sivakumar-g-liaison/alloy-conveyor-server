/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.grammer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.liaison.mailbox.enums.ErrorMessages;

/**
 * A Data Transfer Object that implements the fields required for
 * error message.
 * 
 * @author veerasamyn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace = "http://key.management.liaison.com/error")
public class MailboxServiceError extends DataTransferObject {

	private ErrorMessages errorCode;
    private String errorMessage;

    /**
	 * @return the errorCode
	 */
	public ErrorMessages getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(ErrorMessages errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
