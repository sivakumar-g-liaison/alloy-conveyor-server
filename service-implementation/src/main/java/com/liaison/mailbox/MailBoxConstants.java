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

	/** File datetime format. */
	public final static String FILETIME_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	/** MailBox Sweeper DateTime Format. */
	public final static String MAILBOXSWEEPER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	/** Resolved folder or hidden folder name. */
	public final static String SWEEPED_FOLDER_NAME = "sweeped";

	/** The processed file extension */
	public final static String SWEEPED_FILE_EXTN = ".queued";

	
}
