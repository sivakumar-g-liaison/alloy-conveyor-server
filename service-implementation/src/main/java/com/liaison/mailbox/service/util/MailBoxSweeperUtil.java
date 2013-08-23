/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * MailBoxSweeperUtil
 *
 *<P>Class which has utility methods.
 *
 * @author veerasamyn
 */

public class MailBoxSweeperUtil {

    /**
     * Method to convert given object to JSON string.
     *
     * @param obj
     * @return String which contains JSON of the given object
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    public static String convertObjectToJson(Object obj)
	    throws JsonGenerationException, JsonMappingException, IOException {

	ObjectMapper mapper = new ObjectMapper();
	return mapper.writeValueAsString(obj);

    }

    /**
     * Method is used to get the Date string from the file FileTime.
     *
     * @param timestamp FileTime object
     * @return String which contains formated date time string.
     * @throws Exception
     */
    public static String getDateStringFromFileTime(FileTime timestamp)
	    throws Exception {

	Date date = convertToDate(timestamp.toString(),
		MailBoxSweeperConstants.FILETIME_DATETIME_FORMAT);
	return getDateString(date,
		MailBoxSweeperConstants.MAILBOXSWEEPER_DATETIME_FORMAT);

    }

    /**
     * Method is used to get the given format date string from date obj.
     *
     * @param date
     * @param outputFormat
     * @return String which contains date string of the given format.
     * @throws Exception
     */
    public static String getDateString(Date date, String outputFormat)
	    throws Exception {

	SimpleDateFormat dateFormatter = new SimpleDateFormat(outputFormat);
	String dateString = dateFormatter.format(date);

	return dateString;
    }

    /**
     * Method is used to get the Date from the give string.
     *
     * @param dateTimeString
     * @param inputFormat
     * @return Date which contains formated input dateTimeString.
     * @throws Exception
     */
    public static Date convertToDate(String dateTimeString,
	    String inputFormat) throws Exception {

	SimpleDateFormat dateFormatter = new SimpleDateFormat(inputFormat);
	dateTimeString = dateTimeString.trim();
	return dateFormatter.parse(dateTimeString);


    }

}
