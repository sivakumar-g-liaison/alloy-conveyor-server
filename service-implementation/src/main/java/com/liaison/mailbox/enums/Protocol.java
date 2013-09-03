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

public enum Protocol {

	FTP("ftp"),
	HTTP("http"),
	HTTPS("https");

	private final String code;

	private Protocol(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}

}
