/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * Interface exposes API to run the processors via javascript.
 * 
 * @author OFS
 */
public interface ProcessorJavascriptI {

	/**
	 * This will return a HTTP, FTP, and HTTPS or FTPS client based on the processor type.
	 *
	 * @return
	 * @throws Exception
	 */
	public Object getClient();


	/**
	 * Returns the processor properties as JSON
	 *
	 * @return
	 */
	public String getPropertiesJson();

	/**
	 * Returns array of files from the configured payload location. It reads from the local directory.
	 *
	 * @return
	 */
	public File[] getFilesToUpload() throws MailBoxServicesException, IOException;

	/**
	 * Returns the location to write the payload
	 *
	 * @return
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	public String getWriteResponseURI() throws MailBoxServicesException, IOException;

	/**
	 * Writes the response stream to the file.
	 * The file name format would be "PROCESSOR_NAME + System.nanoTime()"
	 *
	 * @param response
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void writeResponseToMailBox(ByteArrayOutputStream response) throws URISyntaxException, IOException, FS2Exception, MailBoxServicesException;

	/**
	 * Writes the response stream to the file using the given file name.
	 *
	 * @param response
	 * @param filename
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void writeResponseToMailBox(ByteArrayOutputStream response, String filename) throws URISyntaxException, IOException, FS2Exception, MailBoxServicesException;

	/**
	 * Adds or Updates the dynamic properties to a processor. The input should be a Json string which should contain name value pair.
	 *
	 * @param dynamicProperties
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void addorUpdateCustomProperty(String dynamicProperties) throws JAXBException, IOException;

	/**
	 * Returns the list of custom properties of the processor known only to java script
	 *
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public Properties getCustomProperties();

	/**
	 * Returns array of credentials of the processor
	 *
	 * @return
	 */
	public CredentialDTO[] getCredentials() throws MailBoxConfigurationServicesException, SymmetricAlgorithmException;

	/**
	 * Send email notifications
	 *
	 * @param toEmailAddrList
	 *            The extra receivers. The default receiver will be available in the mailbox.
	 * @param subject
	 *            The notification subject
	 * @param emailBody
	 *            The body of the notification
	 * @param type
	 *            The notification type(TEXT/HTML).
	 */
	public void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type);
	
	/**
     * Send email notifications
     *
     * @param toEmailAddrList
     *            The extra receivers. The default receiver will be available in the mailbox.
     * @param subject
     *            The notification subject
     * @param emailBody
     *            The body of the notification
     * @param type
     *            The notification type(TEXT/HTML).
     * @param isOverwrite
     *            To overwrite the configured mail address in the mailbox.        
     */
	public void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type, boolean isOverwrite);

	/**
	 * Returns the properties of the Mailbox
	 *
	 * @return
	 */
	public Properties getMailBoxProperties();


	/**
	 * To Retrieve the Payload URI
	 *
	 * @return Payload URI String
	 * @throws MailBoxConfigurationServicesException
	 * @throws MailBoxServicesException
	 */
	public String getPayloadURI() throws MailBoxServicesException, IOException;

	/**
	 * Method to do clean up activities once JS completes the execution of a processor
	 *
	 */
	public void cleanup();

	/**
	 * Info level logging
	 *
	 * @param msg
	 */
	public void logInfo(String msg);

	/**
	 * Error level logging
	 *
	 * @param msg
	 */
	public void logError(String msg);

	/**
	 * Error level logging
	 *
	 * @param error
	 */
	public void logError(Object error);

	/**
	 * Error level logging
	 *
	 * @param error
	 */
	public void logError(Throwable error);

	/**
	 * Debug level logging
	 *
	 * @param msg
	 */
	public void logDebug(String msg);

	/**
     * Method to logs the TVAPI status and activity messages to LENS
     *
     * @param msg Message String to be logged in LENS event log
     * @param file File corresponding to the log
     * @param status Status of the LENS logging
     */
    public void logToLens(String msg, File file, ExecutionState status);


}
