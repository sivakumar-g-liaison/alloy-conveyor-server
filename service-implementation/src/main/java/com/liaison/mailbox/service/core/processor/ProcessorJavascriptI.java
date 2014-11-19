/**
 *
 */
package com.liaison.mailbox.service.core.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.Logger;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.service.dto.configuration.CredentialDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * @author VNagarajan
 *
 */
public interface ProcessorJavascriptI {

	/**
	 * This will return a HTTP, FTP, and HTTPS or FTPS client based on the processor type.
	 *
	 * @return
	 * @throws Exception
	 */
	Object getClient() throws Exception;


	/**
	 * Returns the processor properties as JSON
	 *
	 * @return
	 */
	String getPropertiesJson();

	/**
	 * Returns array of files from the configured payload location. It reads from the local directory.
	 *
	 * @return
	 */
	File[] getFilesToUpload() throws MailBoxServicesException, IOException;

	/**
	 * Returns the location to write the payload
	 *
	 * @return
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	String getWriteResponseURI() throws MailBoxServicesException, IOException;

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
	void writeResponseToMailBox(ByteArrayOutputStream response) throws URISyntaxException, IOException, FS2Exception, MailBoxServicesException;

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
	void writeResponseToMailBox(ByteArrayOutputStream response, String filename) throws URISyntaxException, IOException, FS2Exception, MailBoxServicesException;

	/**
	 * Adds or Updates the dynamic properties to a processor. The input should be a Json string which should contain name value pair.
	 *
	 * @param dynamicProperties
	 * @throws IOException
	 * @throws JAXBException
	 */
	void addorUpdateCustomProperty(String dynamicProperties) throws JAXBException, IOException;

	/**
	 * Returns the list of custom properties of the processor known only to java script
	 *
	 * @return
	 */
	Properties getCustomProperties();

	/**
	 * TODO
	 */
	void updateState();

	/**
	 * Returns array of credentials of the processor
	 *
	 * TODO How to returns the keys??
	 * @return
	 */
	CredentialDTO[] getCredentials() throws MailBoxConfigurationServicesException, SymmetricAlgorithmException;

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
	void sendEmail(List<String> toEmailAddrList, String subject, String emailBody, String type);

	/**
	 * Returns the properties of the Mailbox
	 *
	 * @return
	 */
	Properties getMailBoxProperties();

}
