/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox;

/**
 * 
 * 
 * @author veerasamyn
 */
public interface MailBoxConstants {

	// Key descriminator values
	/** File datetime format. */
	public final static String FILETIME_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	/** MailBox Sweeper DateTime Format. */
	public final static String MAILBOXSWEEPER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	/** Resolved folder or hidden folder name. */
	public final static String SWEEPED_FOLDER_NAME = "sweeped";

	/** The processed file extension */
	public final static String SWEEPED_FILE_EXTN = ".queued";

	/** Property which is used to get the configured receivers from mailbox. */
	public final static String MBX_RCVR_PROPERTY = "emailnotificationids";

	public final static String MBX_STATUS = "MailBoxStatus";

	public final static String PROCESSOR_TYPE = "ProcessorType";

	public final static String PROCESSOR_PROTOCOL = "Protocol";

	public final static String FOLDER_TYPE = "FolderType";

	public final static String CREDENTIAL_TYPE = "CredentialType";

	public final static String INCOMPLETE_STATUS = "INCOMPLETE_CONFIGURATION";

	public final static String GROUPING_JS_PROP_NAME = "filegroupingjspath";

	public final static String FILE_RENAME_FORMAT_PROP_NAME = "filerenameformat";
	public final static String SWEEPED_FILE_LOCATION = "sweepedfilelocation";
	public final static String PROCESSED_FILE_LOCATION = "processedfilelocation";
	public final static String LAST_MODIFIED_TOLERANCE = "last.modified.tolerance";

	public final static String FOLDER_HEADER = "folderlocation";
	public final static String FILE_NAME_HEADER = "filename";

	public final static String URL = "resturl";

	// Added for list files in get payload
	public final static String META_FILE_NAME = ".meta";
	public final static String PROCESSED_FOLDER = "PROCESSED";

	// used for write response for http remote downloader. This is the default
	// processor name.
	public final static String PROCESSOR = "PROCESSOR";

	// Properties for sweeper grouping boundary condition
	public final static String PAYLOAD_SIZE_THRESHOLD = "payloadsizethreshold";
	public final static String NUMER_OF_FILES_THRESHOLD = "numoffilesthreshold";
	
	// Added for processing Mount Location from Folder Path given by User
	public final static String MOUNT_LOCATION = "MOUNT_POINT";
	public final static String MOUNT_LOCATION_PATTERN = "(?i:MOUNT_POINT)";
	
	// Added for Error File Location
	public final static String ERROR_FOLDER = "ERROR";
	public final static String ERROR_FILE_LOCATION = "errorfilelocation";
	
	// Type of Credentials
	public final static String TRUSTSTORE_CERT = "truststore_cert";
	public final static String SSH_KEYPAIR = "ssh_keypair";
	public final static String LOGIN_CREDENTIAL = "login_credential";
	
	// Type of Truststore
	public final static String GLOBAL = "GLOBAL";
	public final static String SELFSIGNED = "SELFSIGNED";
	
	// Added to acl Manifest Request construction
	public final static String DOMAIN_NAME = "SERVICE_BROKER";
	public final static String DOMAIN_TYPE = "ORGANIZATION";
	public final static String PLATFORM_NAME = "SERVICE_BROKER";
	public final static String SERVICE_NAME = "KEYMANAGER";
	public final static String ROLE_NAME = "MailboxAdmin";
	
	// retrieval of httplistener specific properties
	public static final String HTTPLISTENER_AUTH_CHECK = "httplistenerauthcheckrequired";
	public static final String HTTPLISTENER_PIPELINEID = "httplistenerpipelineid";
	public static final String HTTPLISTENER_PAYLOAD_LOCATION = "httplistenerpayload";
	
	// properties used to configure sla rules of an mailbox
	public final static String TIME_TO_PICK_UP_FILE_POSTED_TO_MAILBOX = "timetopickupfilepostedtomailbox";
	public final static String TIME_TO_PICK_UP_FILE_POSTED_BY_MAILBOX = "timetopickupfilepostedbymailbox";
	
	// sla validation
	public final static String PROCESSOR_NOT_AVAILABLE = "processor_not_available";
	public final static String PROFILE_NOT_AVAILABLE = "profile_not_available";
	public final static String DUMMY_MAILBOX_ID_FOR_FSM_STATE =   "ABCDEFGHIJKLMNOPQRSTUVWXYZ789101";
	public final static String DUMMY_PROCESSOR_ID_FOR_FSM_STATE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";
	
	// spectrum payload prefix
	public final static String SPECTRUM_PAYLOAD_PREFIX = "/mailboxsweeper/payload/1.0/";
	
	// foldername key in additional context of Sweeper DTO ticket
	public final static String FOLDER_NAME = "folderdername";
	
}
