/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Simple class to encapsulate logging of timestamps.
 * 
 * @author OFS
 */
public class TimestampLogger
{
	protected static final String TIMESTAMP_LOGGER_NAME = "TIMESTAMP_LOGGER";
	protected static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final Logger timestampLogger = LogManager.getLogger(TIMESTAMP_LOGGER_NAME);


	/**
	 * Logs a time stamp with the current date as the time stamp.
	 *
	 * @param timestampName  Name of the time stamp being logged.
	 */
	public static void logTimestamp (String timestampName)
	{
		logTimestamp(timestampName, new Date());
	}

	/**
	 * Logs a time stamp with the caller providing the time stamp.
	 *
	 * @param timestampName  Name of the time stamp being logged.
	 * @param timestamp      When this event occurred.
	 */
	public static void logTimestamp (String timestampName, Date timestamp)
	{
		timestampLogger.info(formatDate(timestamp) + ": " + timestampName);
	}

	protected static String formatDate (Date date)
	{
		// Simple Date Format class is not thread safe, so we must either construct one each time this method
		// is called or make the method synchronized and have a global instance.
		// For now, constructing on each call.
		SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
		return sdf.format(date);
	}
}
