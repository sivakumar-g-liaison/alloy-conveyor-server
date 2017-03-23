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
 * Contains the list of deployment type.
 *
 */
public enum DeploymentType {

	RELAY("RELAY"),
	LOWSECURE_RELAY("LOWSECURE_RELAY"),
	CONVEYOR_SERVER("CONVEYOR_SERVER");

	private String value;

	private DeploymentType(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
