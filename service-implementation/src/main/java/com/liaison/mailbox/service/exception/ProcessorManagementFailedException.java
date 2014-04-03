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
 * Customized exception for ProcessorManagementFailedException service operations.
 * 
 * @author santoshc
 */
public class ProcessorManagementFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ProcessorManagementFailedException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ProcessorManagementFailedException(Messages message, String key) {
		super(String.format(message.value(), key));
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ProcessorManagementFailedException(Messages message) {
		super(message.value());
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ProcessorManagementFailedException(String message) {
		super(message);
	}

}

