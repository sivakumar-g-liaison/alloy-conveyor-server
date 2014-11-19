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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.codehaus.jettison.json.JSONException;

import com.google.gson.JsonParseException;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.FSMEventDAOBase;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Http remote uploader to perform push operation, also it has support methods for JavaScript.
 *
 * @author veerasamyn
 */
public class HttpRemoteUploader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(HttpRemoteUploader.class);

	@SuppressWarnings("unused")
	private HttpRemoteUploader() {
		// to force creation of instance only by passing the processor entity
	}

	public HttpRemoteUploader(Processor configurationInstance) {
		super(configurationInstance);
	}

	/**
	 * Java method to execute the HTTPRequest and write in FS location
	 *
	 * @throws MailBoxServicesException
	 * @throws FS2Exception
	 * @throws IOException
	 * @throws LiaisonException
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws SymmetricAlgorithmException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws BootstrapingFailedException
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 *
	 * @throws MailBoxConfigurationServicesException
	 *
	 */
	public void executeRequest(String executionId,MailboxFSM fsm) {

		HTTPRequest request = null;
		HTTPResponse response = null;
		boolean failedStatus = false;

		try {

			RemoteProcessorPropertiesDTO remoteProcessorProperties = getProperties();

			// Set the pay load value to http client input data for POST & PUT request
			File[] files = null;
			if ("POST".equals(remoteProcessorProperties.getHttpVerb())
					|| "PUT".equals(remoteProcessorProperties.getHttpVerb())) {

				files = getFilesToUpload();
				if (null != files) {

					FSMEventDAOBase eventDAO = new FSMEventDAOBase();
					Date lastCheckTime = new Date();
					String constantInterval = MailBoxUtil.getEnvironmentProperties().getString("check.for.interrupt.signal.frequency.in.sec");

					for (File entry : files) {

						//interrupt signal check
						if(((new Date().getTime() - lastCheckTime.getTime())/1000) > Long.parseLong(constantInterval)) {
							lastCheckTime = new Date();
							if(eventDAO.isThereAInterruptSignal(executionId)) {
								LOGGER.info("##########################################################################");
								LOGGER.info("The executor with execution id  "+executionId+" is gracefully interrupted");
								LOGGER.info("#############################################################################");
								fsm.createEvent(ExecutionEvents.INTERRUPTED, executionId);
								fsm.handleEvent(fsm.createEvent(ExecutionEvents.INTERRUPTED));
								return;
							}
						}

						try (InputStream contentStream = FileUtils.openInputStream(entry);
								ByteArrayOutputStream responseStream = new ByteArrayOutputStream(4096)) {

						    request = (HTTPRequest) getClient();
		                    request.setOutputStream(responseStream);

						    request.inputData(contentStream, remoteProcessorProperties.getContentType());

		                    response = request.execute();
		                    LOGGER.info("The reponse code received is {} for a request {} ", response.getStatusCode(), entry.getName());
		                    if (response.getStatusCode() != 200) {

		                        LOGGER.info("The reponse code received is {} ", response.getStatusCode());
		                        LOGGER.info("Execution failure for ",entry.getAbsolutePath());

		                        failedStatus = true;
		                        delegateArchiveFile(entry, MailBoxConstants.ERROR_FILE_LOCATION, true);
		                        //continue;

		                    } else {

		                        if (null != entry) {
		                            delegateArchiveFile(entry, MailBoxConstants.PROCESSED_FILE_LOCATION, false);
		                        }
		                    }
						}

					}

					if (failedStatus) {
						throw new MailBoxServicesException(Messages.HTTP_REQUEST_FAILED, Response.Status.BAD_REQUEST);
					}
				} else {
					LOGGER.info("The given HTTP Uploader payload URI is Empty.");
					throw new MailBoxServicesException("The given payload configuration is Empty.", Response.Status.CONFLICT);
				}
			}
		} catch (JAXBException | IOException | LiaisonException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Delegate method to archive the file.
	 *
	 * @param file
	 * @param locationName
	 * @param isError
	 * @throws IOException
	 */
	private void delegateArchiveFile(File file, String locationName, boolean isError) throws IOException {

		String fileLocation = replaceTokensInFolderPath(getCustomProperties().getProperty(locationName));
		if (MailBoxUtil.isEmpty(fileLocation)) {
			archiveFile(file.getAbsolutePath(), isError);
		} else {
			archiveFile(file, fileLocation);
		}
	}

	@Override
	public void invoke(String executionId,MailboxFSM fsm) {

		// HTTPRequest executed through JavaScript
		if (!MailBoxUtil.isEmpty(configurationInstance.getJavaScriptUri())) {
			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			JavaScriptUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

		} else {
			// HTTPRequest executed through Java
			executeRequest(executionId, fsm);
		}
	}

	protected boolean checkFileExistence() {
		// TODO: Implementation Logic for file existence
		return false;
	}

	@Override
	public Object getClient() {
		return ClientFactory.getClient(this);
	}
}
