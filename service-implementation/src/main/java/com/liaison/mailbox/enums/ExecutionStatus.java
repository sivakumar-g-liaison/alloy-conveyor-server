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
public enum ExecutionStatus {

	READY("ready"),
	RUNNING("running"),
	FAILED("failed"),
	COMPLETED("completed");

	private String value;

	private ExecutionStatus(String status) {
		this.value = status;
	}

	public String value() {
		return value;
	}

	public static ExecutionStatus findByCode(String code) {

		ExecutionStatus found = null;
		for (ExecutionStatus value : ExecutionStatus.values()) {

			if (!MailBoxUtility.isEmpty(code) && code.equals(value.value())) {
				found = value;
				break;
			}
		}

		return found;
	}

	public static ExecutionStatus findByName(String name) {

		ExecutionStatus found = null;
		for (ExecutionStatus value : ExecutionStatus.values()) {

			if (!MailBoxUtility.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
