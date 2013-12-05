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

	public final static String INCOMPLETE_STATUS = "INCOMPLETE";

	public final static String GROUPING_JS_PROP_NAME = "filegroupingjspath";

	public final static String FILE_RENAME_FORMAT_PROP_NAME = "filerenameformat";
	public final static String SWEEPED_FILE_LOCATION = "sweepedfilelocation";
	public final static String PROCESSED_FILE_LOCATION = "processedfilelocation";

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

}
