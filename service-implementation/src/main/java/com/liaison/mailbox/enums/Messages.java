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
	PROFILE_DOES_NOT_EXIST("Profile with the given id %s does not exist."),
	PROFILE_NAME_DOES_NOT_EXIST("Profile with the given name %s does not exist."),
	PROCESSOR_DOES_NOT_EXIST("Processor with the given id %s does not exist."),
	MBX_PROFILE_LINK_DOES_NOT_EXIST("Mailbox-Profile link with the given id %s does not exist."),
	MBX_PROFILE_LINK_DOES_NOT_MATCH("Mailbox-Profile link with the given id %s does not match the given Mailbox."),
	PROFILE_ALREADY_EXISTS("Profile Already exists"),
	ENTITY_ALREADY_EXIST("%s already exists in the system."),

	// COMMON MESSAGES
	CREATE_OPERATION_FAILED("%s creation failed."),
	REVISE_OPERATION_FAILED("%s revise operation failed."),
	READ_OPERATION_FAILED("Failed to retrieve the %s."),
	SEARCH_OPERATION_FAILED("%s search operation failed."),
	RECEIVED_OPERATION_FAILED("%s receive operation failed."),
	DEACTIVATION_FAILED("%s deactivation failed."),
	CREATED_SUCCESSFULLY("%s created successfully."),
	RECEIVED_SUCCESSFULLY("%s received successfully."),
	REVISED_SUCCESSFULLY("%s revised successfully."),
	READ_SUCCESSFUL("%s read successfully."),
	SEARCH_SUCCESSFUL("%s searched successfully."),
	DEACTIVATION_SUCCESSFUL("%s deactivated successfully."),
	INVALID_DATA("Mailbox name or profile name is mandatory to enable search"),
	NO_COMPONENT_EXISTS("No %s available in the system."),
	NO_SUCH_COMPONENT_EXISTS("No such %s available in the system."),
	SUCCESS("success"),
	FAILURE("failure"),
	MANDATORY_FIELD_MISSING("%s is mandatory"),
	INVALID_REQUEST("The input request is invalid."),
	GUID_NOT_AVAIL("Input Id is not available in the system."),
	GUID_DOES_NOT_MATCH("%s Id in the request does not match the resource."),
	CERTIFICATE_RETRIEVE_FAILED("Unable to retrieve the certificate"),
	SSHKEY_RETRIEVE_FAILED("Unable to retrieve the SSH key"),
	INVALID_DATE_RANGE("Both From and To dates are required."),
	INVALID_PROCESSOR_STATUS("Processor status is invalid."),
	NO_PROCESSORS_AVAIL("No Processors available matching the given criteria."),

	// TRIGGER PROFILE USECASE
	PROFILE_TRIGGERED_SUCCESSFULLY("Processors matching profile %s triggered successfully."),
	NO_PROC_CONFIG_PROFILE("There are no processors configured for this profile or the only processor configured is already running."),
	TRG_PROF_FAILURE("Error triggering the profile %s ."),
	FOLDERS_CONFIGURATION_INVALID("Folders configured for processors, but they are missing folder type and folder uri."),
	CREDENTIAL_CONFIGURATION_INVALID("Credential configured for processors, but they are missing Credetial type and Credetial uri."),
	CREDENTIAL_URI_INVALID("Credential configured for processors, but Credetial uri is invalid."),
	INJECTION_OF_PROPERTIES_FAILED("Injection of properties while building http client from processor configuration failed."),
	HTTP_REQUEST_FAILED("HTTP request failed."),
	RESPONSE_LOCATION_NOT_CONFIGURED("The response location is not configured or empty."),
	PAYLOAD_LOCATION_NOT_CONFIGURED("The payload location is not configured or empty."),
	INVALID_DIRECTORY("The given directory is not available in the system."),

	PROCESSOR_EXECUTION_SUCCESSFULLY("Processor %s execution successfully."),
	PROCESSOR_EXECUTION_FAILED("Error execution in the processor %s."),

	// INVALID ENUM
	ENUM_TYPE_DOES_NOT_SUPPORT("%s type is set to a value that is not supported."),

	// MBX GUID DOES NOT MATCH WITH THE GIVEN PROCESSOR
	PROC_DOES_NOT_BELONG_TO_MBX("The given processor does not belong to given mailbox."),

	ERROR_MSG("Validation failed with the following error(s):"),
	VALIDATOR_ERROR_MSG("Error in validator, object cannot be null."),

	// HTTP SERVER LISTENER
	EMPTY_VALUE("The given %s is empty."),
	PERSIST_SUCCESS("%s persisted successfully."),
	PERSIST_FAILURE("Failed to persist the %s."),

	// User Account Messages
	INVALID_ACC_REQUEST("The input request is invalid."),
	ACC_DOES_NOT_EXIST("User Account with the given id %s does not exist."),
	DATA_PREPOPULATE("Data populated successfully"),

	// KMS Messages
	SELFSIGNED_TRUSTSTORE_CREATION_FAILED("SelfSigned TrustStore Creation Failed"),

	//getting java properties
	READ_JAVA_PROPERTIES_SUCCESSFULLY("%s properties read successfully."),
	READ_JAVA_PROPERTIES_FAILED("%s properties read failed."),

	//retrieving tenancy key from acl manifest
	TENANCY_KEY_RETRIEVAL_FAILED("Manifest does not contain the Tenancy Key for mailbox."),
	TENANCY_KEY_MISMATCH("Tenancy Key present in Manifest does not match the Tenancy Key of mailbox."),
	ACL_MANIFEST_NOT_AVAILABLE("ACL Manifest is not available in the %s."),

	READ_SECRET_FAILED("Key manager failed to retrieve stored secret"),

	//retrieving service instance id from query parameter
	SERVICE_INSTANCE_ID_NOT_AVAILABLE("Service Instance ID is not available as query parameter."),

	// Mailbox Expectation Management
	MAILBOX_ADHERES_SLA("Mailbox adheres to SLA rules"),
	MAILBOX_DOES_NOT_ADHERES_SLA("Mailbox does not adhere to SLA rules.Mailboxes which violated SLA rules are "),
	UPLOADER_NOT_AVAILABLE("Mailbox %s does not contain processor of type uploader"),
	FAILED_TO_VALIDATE_SLA("Failed to validate sla of %s"),
	PROFILE_NOT_CONFIGURED("The profile is not configured."),

	// runtime DB changes
	INVALID_PROCESSOR_EXECUTION_STATUS("The processor execution status is invalid"),

	// Manifest validation
	DOMAIN_INTERNAL_NAME_MISSING_IN_MANIFEST("The acl manifest must contain the Domain Internal Name."),
	
	//sync and async process
	MISSING_PROCESSOR("Either the mailbox is not available in the system or the %s is not found for this id.");
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
