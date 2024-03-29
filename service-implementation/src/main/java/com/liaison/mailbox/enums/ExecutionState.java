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
 * Contains the list of state for all executions.
 * 
 * @author OFS
 */
public enum ExecutionState {

	READY("READY","The Processor is ready"),
	PROCESSING("PROCESSING","The processor is currently in progress"),
	FAILED("FAILED","Processor execution failed"),
	COMPLETED("COMPLETED","Processor execution completed"),
	QUEUED("QUEUED","Processor is queued for execution"),
	STAGED("STAGED","File is staged"),
	STAGING_FAILED("STAGING_FAILED", "File Staging failed"),
	DUPLICATE("DUPLICATE", "File isn't staged because duplicate file exists at the target location"),
	HANDED_TO_JS("HANDED_TO_JS","Processor Execution Handed over to JS"),
	INTERRUPTED("INTERRUPTED","Processor is gracefully Interrupted"),
	SKIPPED("SKIPPED","Processor is already running so skipped execution"),
	VALIDATION_ERROR("VALIDATION_ERROR","Validation failed");
	
	private String value;
	private String notes;

	private ExecutionState(String status,String notes) {
		this.value = status;
		this.notes = notes;
	}

	public String value() {
		return value;
	}
	
	public String notes() {
		return notes;
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
