/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.enums;

import javax.xml.bind.annotation.XmlEnum;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a set of error codes and their English meaning.
 *
 * @author veerasamyn
 */
@XmlEnum
public enum Messages {

    MBX_DOES_NOT_EXIST("Mailbox with the given id %s does not exist."),
    UPLOADED_FILE_DOES_NOT_EXIST("Uploaded file with the given id %s does not exist."),
    PROFILE_DOES_NOT_EXIST("Profile with the given id %s does not exist."),
    PROFILE_NAME_DOES_NOT_EXIST("Profile with the given name %s does not exist."),
    PROCESSOR_DOES_NOT_EXIST("Processor with the given id %s does not exist."),
    PROCESSOR_TYPE_REVISION_NOT_ALLOWED("Processor type revision is not allowed"),
    PROCESSOR_PROTOCOL_REVISION_NOT_ALLOWED("Processor protocol revision is not allowed"),
    MBX_PROFILE_LINK_DOES_NOT_EXIST("Mailbox-Profile link with the given id %s does not exist."),
    MBX_PROFILE_LINK_DOES_NOT_MATCH("Mailbox-Profile link with the given id %s does not match the given Mailbox."),
    PROFILE_ALREADY_EXISTS("Profile Already exists"),
    ENTITY_ALREADY_EXIST("%s already exists in the system."),
    PWD_EMPTY("Password cannot be Empty."),
    PASSWORD_OR_SSH_KEYPAIR_EMPTY("Either the Password or the SSH Keypair is Empty."),
    USERNAME_EMPTY("Username cannot be Empty."),
    PWD_INVALID("The given secret guid does not exist in the key management system."),
    PWD_ENCODE_INVALID("The secret from key management system is not base64 encoded."),
    PROCESSOR_NOT_ALLOWED("Processor type or cluster type and protocol type is not allowed for deployment type %s"),
    PROCESSOR_PROFILE_NOT_ALLOWED("Profile is not allowed for processor type %s"),
    MBX_NON_DELETED_PROCESSOR("There are active processors available for the Mailbox"),
    DELETE_OPERATION_NOT_ALLOWED("Delete operation is not allowed in revise request"),
    INVALID_PROTOCOL_ERROR_MESSAGE(" Protocol %s is not allowed for the processor type %s"),
    INVALID_PROCESS_TYPE_TO_UPDATE_DC(" Processor DC update is not supported for the processor type %s"),
    INVALID_PROCESS_DC(" Given Process DC %s is invalid"),
    INVALID_HEADER_INFORMATION("Content type and payload are not available in the given request"),

    // generic messages
    CREATE_OPERATION_FAILED("%s creation failed."),
    REVISE_OPERATION_FAILED("%s revise operation failed."),
    READ_OPERATION_FAILED("Failed to retrieve the %s."),
    SEARCH_OPERATION_FAILED("%s search operation failed."),
    RECEIVED_OPERATION_FAILED("%s receive operation failed."),
    DEACTIVATION_FAILED("%s deactivation failed."),
    CREATED_SUCCESSFULLY("%s created successfully."),
    RECEIVED_SUCCESSFULLY("%s received successfully."),
    REVISED_SUCCESSFULLY("%s revised successfully."),
    DELETED_SUCCESSFULLY("%s deleted successfully."),
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
    GUID_NOT_AVAIL("Input id is not available in the system."),
    GUID_DOES_NOT_MATCH("%s id in the request does not match the resource."),
    CERTIFICATE_RETRIEVE_FAILED("Unable to retrieve the certificate"),
    SSHKEY_RETRIEVE_FAILED("Unable to retrieve the SSH key"),
    INVALID_DATE_RANGE("Both From and To dates are required."),
    INVALID_PROCESSOR_STATUS("Processor status is invalid."),
    NO_PROCESSORS_AVAIL("No Processors available matching the given criteria."),
    PROCESSORS_LIST("The list of running processors are "),
    NO_EXECUTING_PROCESSORS_AVAIL("No %s available"),
    PROPERTY_DUPLICATE_ERROR("Property name should always be unique"),
    INVALID_CONNECTION_TIMEOUT("Invalid value for timeout"),

    // trigger profile use case
    PROFILE_TRIGGERED_SUCCESSFULLY("Processors matching profile %s triggered successfully."),
    NO_PROC_CONFIG_PROFILE("There are no processors configured for this profile or the only processor configured is already running."),
    TRG_PROF_FAILURE("Error triggering the profile %s ."),
    FOLDERS_CONFIGURATION_INVALID("Folders configured for processors, but they are missing folder type and folder uri."),
    CREDENTIAL_CONFIGURATION_INVALID("Credential configured for processors, but they are missing Credetial type and Credetial uri."),
    CREDENTIAL_URI_INVALID("Credential configured for processors, but Credetial uri is invalid."),
    INJECTION_OF_PROPERTIES_FAILED("Injection of properties while building http client from processor configuration failed."),
    HTTP_REQUEST_FAILED("HTTP request failed."),
    INVALID_DIRECTORY("The given directory is not available in the system."),

    PROCESSOR_EXECUTION_SUCCESSFULLY("Processor %s execution successfully."),
    PROCESSOR_EXECUTION_FAILED("Error execution in the processor %s."),

    // create configured location
    LOCAL_FOLDERS_CREATION_FAILED("Local folder %s creation failed."),
    HOME_FOLDER_DOESNT_EXIST_ALREADY("The local folder %s must exist before creating custom folders."),
    FOLDER_DOESNT_MATCH_PATTERN("Application can create folders only if it matches the pattern %s ."),

    // invalid enum
    ENUM_TYPE_DOES_NOT_SUPPORT("%s type is set to a value that is not supported."),

    // mbx guid does not match with the given processor
    PROC_DOES_NOT_BELONG_TO_MBX("The given processor does not belong to given mailbox."),

    ERROR_MSG("Validation failed with the following error(s):"),
    VALIDATOR_ERROR_MSG("Error in validator, object cannot be null."),

    // http server listener
    EMPTY_VALUE("The given %s is empty."),
    PERSIST_SUCCESS("%s persisted successfully."),
    PERSIST_FAILURE("Failed to persist the %s."),

    // user account messages
    INVALID_ACC_REQUEST("The input request is invalid."),
    ACC_DOES_NOT_EXIST("User Account with the given id %s does not exist."),
    DATA_PREPOPULATE("Data populated successfully"),

    // kms messages
    SELFSIGNED_TRUSTSTORE_CREATION_FAILED("SelfSigned TrustStore Creation Failed"),

    // getting java properties
    READ_JAVA_PROPERTIES_SUCCESSFULLY("%s properties read successfully."),
    READ_JAVA_PROPERTIES_FAILED("%s properties read failed."),

    // retrieving tenancy key from acl manifest
    TENANCY_KEY_RETRIEVAL_FAILED("Manifest does not contain the Tenancy Key for mailbox."),
    INVALID_TENANCY_KEY("Tenancy Key is not available in Global Enterprise Manager"),
    TENANCY_KEY_NOT_AVAILABLE("Tenancy Key is not available in the request."),
    ACL_MANIFEST_NOT_AVAILABLE("ACL Manifest is not available in the request header."),
    TENANCY_KEY_UPDATE_NOT_ALLOWED("Tenancy Key update not allowed."),

    READ_SECRET_FAILED("Key manager failed to retrieve stored secret"),

    // retrieving service instance id from query parameter
    SERVICE_INSTANCE_ID_NOT_AVAILABLE("Service Instance ID is not available as query parameter."),

    // validating service instance Id
    ID_IS_INVALID("%s ID is not available in service broker."),

    // mailbox expectation management
    MAILBOX_ADHERES_SLA("Mailbox adheres to SLA rules"),
    MAILBOX_DOES_NOT_ADHERES_SLA("Mailbox does not adhere to SLA rules.Mailboxes which violated SLA rules are "),
    UPLOADER_OR_FILEWRITER_NOT_AVAILABLE("Mailbox %s does not contain processor of type uploader or filewriter"),
    FAILED_TO_VALIDATE_SLA("Failed to validate sla of %s"),
    PROFILE_NOT_CONFIGURED("The profile is not configured."),

    // runtime db changes
    INVALID_PROCESSOR_EXECUTION_STATUS("The processor execution status is invalid"),

    // manifest validation
    DOMAIN_INTERNAL_NAME_MISSING_IN_MANIFEST("The acl manifest must contain the Domain Internal Name."),

    LOCATION_NOT_CONFIGURED("The %s location is not configured or empty."),

    //sync and async process
    MISSING_PROCESSOR("Either the mailbox is not available in the system or the %s is not found for this mailbox."),

    //Inactive mailbox/processor
    INACTIVE_ENTITY("%s is in-active in the system."),
    
    AUTHENTICATION_SUCCESSFULL("%s authenticated successfully."),
    AUTHENTICATION_FAILURE("Failed to authenticate user."),
    AUTH_AND_GET_ACL_FAILURE("Failed to authenticate and get user ACL"),
    AUTHENTICATION_SUCCESS("User authenticated successfully."),

    // dropbox processor
    CONTENT_QUEUED_FOR_TRANSFER_SUCCESSFUL("Content queued for transfer successfully."),
    CONTENT_QUEUED_FOR_TRANSFER_FAILED("Content queued for transfer got failed."),
    STAGED_FILEID_DOES_NOT_EXIST("Staged file with the given id %s does not exist."),
    RETRIEVE_SUCCESSFUL("%s retrieved successfully."),
    DELETE_ONDEMAND_SUCCESSFUL("File unstaged successfully"),
    STAGED_FILE_DELETE_SUCCESSFUL("Staged file deleted successfully"),
    USER_AUTHENTICATED_AND_GET_MANIFEST_SUCCESSFUL("User authenticated and manifest retrieved successfully"),
    STAGE_FILEID_NOT_BELONG_TO_ORGANISATION("Given staged file id does not belong to any user organisation."),

    // processing of token for the retrieval of login id or authentication token failed.
    PROCESSING_OF_TOKEN_FAILED("Token cannot be processed."),
    REQUEST_HEADER_PROPERTIES_MISSING("Request header properties are missing"),

    NO_PROCESSORS_EXIST("No Processors available."),
    NO_MBX_NAMES_EXIST("No Mailbox names available."),
    NO_PROC_NAMES_EXIST("No Processor names available."),

    COMMON_SYNC_ERROR_MESSAGE("An error condition has been detected within the system. If this condition persists, please contact your administrator or customer support for more details."),

    PAYLOAD_ALREADY_EXISTS("Failed to persist the payload in fs2 storage because it already exists."),
    PAYLOAD_PERSIST_ERROR("Failed to persist the payload in fs2 storage due to error."),
    PAYLOAD_READ_ERROR("Failed to retrieve payload from fs2 storage due to error"),
    PAYLOAD_HEADERS_READ_ERROR("Failed to retrieve payload headers from fs2 storage due to error"),
    META_DATA_READ_ERROR("Failed to retrieve meta data from fs2 storage due to error"),
    PAYLOAD_DOES_NOT_EXIST("Payload is not available in spectrum."),

    // referred in processor admin details resource
    PROCESSOR_ID_NOT_AVAILABLE("Processor ID is not available as query parameter."),
    PROCESSOR_IDS_NOT_AVAILABLE("Processor IDs are not available."),
    PROCESSOR_EXECUTION_STATE_NOT_EXIST("Processor execution state does not exist for the given id %s ."),
    PROCESSOR_EXECUTION_STATE_NOT_PROCESSING("The status for the given processor id %s is not updated since the execution is not in 'Processing' state."),

    INVALID_PROCESSOR_ID("The given processor %s does not exist in the RuntimeProcessors."),
    
    FILE_WRITER_SUCCESS_MESSAGE("File is picked up by the customer or other process"),
    INVALID_FILE_NAME("File name is invalid"),
    PROCESS_DC_EMPTY("Process DC value cannot be empty");

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
