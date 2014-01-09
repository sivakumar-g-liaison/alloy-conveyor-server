/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.enums;

import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * 
 * 
 * @author veerasamyn
 */
public enum ExecutionEvents {

	PROCESSOR_EXECUTION_STARTED("PROCESSOR_EXECUTION_STARTED"),
	PROCESSOR_EXECUTION_FAILED("PROCESSOR_EXECUTION_FAILED"),
	PROCESSOR_EXECUTION_COMPLETED("PROCESSOR_EXECUTION_COMPLETED"),
	PROCESSOR_QUEUED("PROCESSOR_QUEUED"),
	FILE_STAGED("STAGED");

	private String value;

	private ExecutionEvents(String status) {
		this.value = status;
	}

	public String value() {
		return value;
	}

	public static ExecutionEvents findByCode(String code) {

		ExecutionEvents found = null;
		for (ExecutionEvents value : ExecutionEvents.values()) {

			if (!MailBoxUtility.isEmpty(code) && code.equals(value.value())) {
				found = value;
				break;
			}
		}

		return found;
	}

	public static ExecutionEvents findByName(String name) {

		ExecutionEvents found = null;
		for (ExecutionEvents value : ExecutionEvents.values()) {

			if (!MailBoxUtility.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
