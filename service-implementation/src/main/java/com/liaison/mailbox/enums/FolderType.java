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
 * @author praveenu
 * 
 */
public enum FolderType {

	PAYLOAD_LOCATION("PAYLOAD_LOCATION"), RESPONSE_LOCATION("RESPONSE_LOCATION");

	private final String code;

	private FolderType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}

}
