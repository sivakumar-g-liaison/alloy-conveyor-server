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
 * @author OFS
 */
public enum DeploymentType {

	RELAY("RELAY"),
	LOWSECURE_RELAY("LOWSECURE-RELAY"),
	CONVEYOR("CONVEYOR");

	private String value;

	private DeploymentType(String status) {
		this.setValue(status);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
