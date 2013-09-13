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
public enum Messages {
    
	MBX_DOES_NOT_EXIST("Mailbox with the given id %s does not exist."),	
	PROCESSOR_DOES_NOT_EXIST("Processor with the given id %s does not exist."),		
     //COMMON MESSAGES
	CREATE_OPERATION_FAILED("%s creation failed."),
	REVISE_OPERATION_FAILED("% revise operation failed."),
	READ_OPERATION_FAILED("Failed to retrieve the %s."),
	DEACTIVATION_FAILED("%s deactivation failed."),	
	CREATED_SUCCESSFULLY("%s created successfully."),
	REVISED_SUCCESSFULLY("% revised successfully."),
	READ_SUCCESSFUL("%s read successfully."),
	DEACTIVATION_SUCCESSFUL("%s deactivated successfully."),
	SUCCESS("success"),
	FAILURE("failure"),
	MANDATORY_FIELD_MISSING("%s is mandatory"),
	INVALID_REQUEST("The input request is invalid."),
	GUID_NOT_AVAIL("Input Id is not available in the system"),
	GUID_DOES_NOT_MATCH("Id in the request does not match the resource"),
	//TRIGGER PROFILE USECASE
	PROFILE_TRIGGERED_SUCCESSFULLY("Processors matching profile %s triggered successfully"),
	NO_PROC_CONFIG_PROFILE("There are no processors configured for this profile."),
	TRG_PROF_FAILURE("Error triggering the profile %s ");

	private String value;

	// some caching to provide a better search algorithm
	private static Map<String, Messages> values = new HashMap<String, Messages>();
	static {
		for (Messages r : EnumSet.allOf(Messages.class)) {
			values.put(r.toString(), r);
		}
	}

	private Messages(String message) {
		this.value = message;
	}

	public String value() {
		return value;
	}

	
}
