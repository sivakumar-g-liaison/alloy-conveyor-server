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


/**
 * Contains the list of event status for all executions.
 *
 * @author OFS
 */
public enum ExecutionEvents {

	PROCESSOR_EXECUTION_STARTED("PROCESSOR_EXECUTION_STARTED"),
	PROCESSOR_EXECUTION_FAILED("PROCESSOR_EXECUTION_FAILED"),
	PROCESSOR_EXECUTION_COMPLETED("PROCESSOR_EXECUTION_COMPLETED"),
	PROCESSOR_QUEUED("PROCESSOR_QUEUED"),
	FILE_STAGED("STAGED"),
	FILE_STAGING_FAILED("FILE_STAGING_FAILED"),
	PROCESSOR_EXECUTION_HANDED_OVER_TO_JS("PROCESSOR_EXECUTION_HANDED_OVER_TO_JS"),
	INTERRUPT_SIGNAL_RECIVED("INTERRUPT_SIGNAL_RECIVED"),
	INTERRUPTED("INTERRUPTED"),
	GRACEFULLY_INTERRUPTED("GRACEFULLY_INTERRUPTED"),
	SKIP_AS_ALREADY_RUNNING("SKIP_AS_ALREADY_RUNNING");

	private String value;

	private ExecutionEvents(String status) {
		this.setValue(status);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
