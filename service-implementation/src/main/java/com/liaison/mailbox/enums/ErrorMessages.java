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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;

/**
 * This is a set of error codes and their English meaning.
 * 
 * @author veerasamyn
 */
@XmlEnum
public enum ErrorMessages {

	INVALID_REQUEST("The supplied Request is invalid."),
	MISSING_FIELD("A required field is missing."),
	GUID_NOT_AVAIL("The given guid is not availble in the system.");

	private String value;

	// some caching to provide a better search algorithm
	private static Map<String, ErrorMessages> values = new HashMap<String, ErrorMessages>();
	static {
		for (ErrorMessages r : EnumSet.allOf(ErrorMessages.class)) {
			values.put(r.toString(), r);
		}
	}

	
	private ErrorMessages(String algorithm) {
		this.value = algorithm;
	}

	public String value() {
		return value;
	}

}
