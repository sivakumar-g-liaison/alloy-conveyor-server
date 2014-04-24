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

import com.liaison.mailbox.service.util.MailBoxUtil;

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

	/**
	 *This  method will retrieve the ExecutionState by given FSMStateValue value.
	 * 
	 * @param code 
	 * 		  The FSMStateValue value
	 * @return ExecutionState
	 */
	public static ExecutionState findByCode(String code) {

		ExecutionState found = null;
		for (ExecutionState value : ExecutionState.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.value())) {
				found = value;
				break;
			}
		}

		return found;
	}
	
}
