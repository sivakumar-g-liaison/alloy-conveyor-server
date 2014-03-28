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
public enum ExecutionState {

	READY("READY"),
	PROCESSING("PROCESSING"),
	FAILED("FAILED"),
	COMPLETED("COMPLETED"),
	QUEUED("QUEUED"),
	STAGED("STAGED"),
	HANDED_OVER_TO_JS("HANDED_OVER_TO_JS"),
	GRACEFULLY_INTERRUPTED("GRACEFULLY_INTERRUPTED"),
	SKIPPED_SINCE_ALREADY_RUNNING("SKIPPED_SINCE_ALREADY_RUNNING");

	private String value;

	private ExecutionState(String status) {
		this.value = status;
	}

	public String value() {
		return value;
	}

	public static ExecutionState findByCode(String code) {

		ExecutionState found = null;
		for (ExecutionState value : ExecutionState.values()) {

			if (!MailBoxUtility.isEmpty(code) && code.equals(value.value())) {
				found = value;
				break;
			}
		}

		return found;
	}

	public static ExecutionState findByName(String name) {

		ExecutionState found = null;
		for (ExecutionState value : ExecutionState.values()) {

			if (!MailBoxUtility.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
