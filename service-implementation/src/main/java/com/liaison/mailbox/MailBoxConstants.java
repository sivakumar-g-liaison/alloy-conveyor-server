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
	public static final String REMOTE_UPLOADER = "REMOTE_UPLOADER";
	public static final String REMOTE_DOWNLOADER = "REMOTE_DOWNLOADER";
	public static final String SWEEPER = "SWEEPER";

	// Response Messages
	public static final String SUCCESS = "Success";
	public static final String FAILURE = "Failure";

	public static final String CREATE_MAILBOX_SUCCESS = "Mailbox created successfully.";
	public static final String CREATE_MAILBOX_FAILURE = "Error occured during mailbox creation.";

	public static final String ADD_PROFILE_SUCCESS = "Profile added to mailbox Successfully.";
	public static final String ADD_PROFILE_FAILURE = "Error occured while adding Profile to Mailbox.";

	public static final String DEACTIVATE_MAIBOX_PROFILE_SUCCESS = "Mailbox Profile deactivated successfully.";
	public static final String DEACTIVATE_MAIBOX_PROFILE_FAILURE = "Error occured while deactivating Mailbox Profile.";

	public static final String REVISE_MAILBOX_SUCCESS = "Mailbox revised successfully.";
	public static final String REVISE_MAILBOX_FAILURE = "Error occured during mailbox updation.";

	public static final String GET_MAILBOX_SUCCESS = "Mailbox retrieved successfully.";
	public static final String GET_MAILBOX_FAILURE = "Error occured during get mailbox operation.";

	public static final String INACTIVE_MAILBOX_SUCCESS = "Mailbox deactivated successfully.";
	public static final String INACTIVE_MAILBOX_FAILURE = "Error occured during mailbox deactivation.";

	/** File datetime format. */
	public final static String FILETIME_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	/** MailBox Sweeper DateTime Format. */
	public final static String MAILBOXSWEEPER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	/** Resolved folder or hidden folder name. */
	public final static String SWEEPED_FOLDER_NAME = "sweeped";

	/** The processed file extension */
	public final static String SWEEPED_FILE_EXTN = ".queued";

	public static final String CREATE_PROCESSOR_SUCCESS = "Processor created successfully.";
	public static final String CREATE_PROCESSOR_FAILURE = "Error occured during processor creation.";

	public static final String DELETE_PROCESSOR_SUCCESS = "Processor deactivated successfully.";
	public static final String DELETE_PROCESSOR_FAILURE = "Processor deactivation failed.";

	public static final String GET_PROCESSOR_SUCCESS = "Processor retrieved successfully.";
	public static final String GET_PROCESSOR_FAILURE = "Error occured during get processor operation.";
	
	public static final String REVISE_PROCESSOR_SUCCESS = "Processor revised successfully.";
	public static final String REVISE_PROCESSOR_FAILURE = "Error occured during processor updation.";
}
