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
	GUID_NOT_AVAIL("The given guid is not availble in the system."),
	MBX_DOES_NOT_EXIST("No such mailbox exists in the system."),
	GUID_DOES_NOT_MATCH("The given guid does not match with request guid."),
	NO_PROC_CONFIG_PROFILE("There are no processors configured for this profile."),
	TRG_PROF_FAILURE("Error occured while triggering the profile "),

	MAILBOX_CREATE_FAILURE("MailBox creation failed."),
	MAILBOX_REVISE_FAILURE("MailBox revise failed."),
	MAILBOX_GET_FAILURE("Failed to retrieve the MailBox."),
	MAILBOX_DELETE_FAILURE("MailBox deactivation failed.");

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

	public static void main(String[] args) {

		String s = "Profile  profileName  triggered successfully.";
		System.out.println(s.replaceAll("profileName", "test"));
	}
}
