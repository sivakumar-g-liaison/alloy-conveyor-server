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
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.FSMEventDAOBase;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Http remote uploader to perform push operation, also it has support methods
 * for JavaScript.
 *
 * @author veerasamyn
 */
public class HTTPRemoteUploader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(HTTPRemoteUploader.class);

	@SuppressWarnings("unused")
	private HTTPRemoteUploader() {
		// to force creation of instance only by passing the processor entity
	}

	public HTTPRemoteUploader(Processor configurationInstance) {
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
	public void executeRequest(String executionId, MailboxFSM fsm) {

		HTTPRequest request = null;
		HTTPResponse response = null;
		boolean failedStatus = false;

		try {

			HTTPUploaderPropertiesDTO httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) getProperties();

			// Set the pay load value to http client input data for POST & PUT
			// request
			File[] files = null;
			// retrieve required properties
			String httpVerb = httpUploaderStaticProperties.getHttpVerb();
			String contentType = httpUploaderStaticProperties.getContentType();

			LOGGER.info(constructMessage("Start run"));
			long startTime = System.currentTimeMillis();

			if ("POST".equals(httpVerb) || "PUT".equals(httpVerb)) {

				files = getFilesToUpload();
				if (null != files) {

					FSMEventDAOBase eventDAO = new FSMEventDAOBase();
					Date lastCheckTime = new Date();
					String constantInterval = MailBoxUtil.getEnvironmentProperties().getString(
							MailBoxConstants.DEFAULT_INTERRUPT_SIGNAL_FREQUENCY_IN_SEC);

					for (File entry : files) {

						// interrupt signal check has to be done only if
						// execution Id is present
						if (!StringUtil.isNullOrEmptyAfterTrim(executionId)
								&& ((new Date().getTime() - lastCheckTime.getTime()) / 1000) > Long
										.parseLong(constantInterval)) {
							lastCheckTime = new Date();
							if (eventDAO.isThereAInterruptSignal(executionId)) {
								LOGGER.info("##########################################################################");
								LOGGER.info("The executor with execution id  " + executionId
										+ " is gracefully interrupted");
								LOGGER.info("#############################################################################");
								fsm.createEvent(ExecutionEvents.INTERRUPTED, executionId);
								fsm.handleEvent(fsm.createEvent(ExecutionEvents.INTERRUPTED));
								return;
							}
						}

						try (InputStream contentStream = FileUtils.openInputStream(entry);
								ByteArrayOutputStream responseStream = new ByteArrayOutputStream(4096)) {

							LOGGER.info(constructMessage("uploading file {}"), entry.getName());

							request = (HTTPRequest) getClient();
							request.setOutputStream(responseStream);
							request.inputData(contentStream, contentType);

							response = request.execute();
							LOGGER.info(constructMessage("The reponse code received is {} for a request {} "),
							        response.getStatusCode(),
									entry.getName());
							if (response.getStatusCode() != 200) {

								LOGGER.warn(constructMessage("The reponse code received is {} "), response.getStatusCode());
								LOGGER.warn(constructMessage("Execution failure for "), entry.getAbsolutePath());

								failedStatus = true;
								delegateArchiveFile(entry, MailBoxConstants.PROPERTY_ERROR_FILE_LOCATION, true);

								String msg = "Failed to upload a file " + entry.getName();
								logToLens(msg, entry, ExecutionState.FAILED);
								// continue;

							} else {
								totalNumberOfProcessedFiles++;
								if (null != entry) {
									delegateArchiveFile(entry, MailBoxConstants.PROPERTY_PROCESSED_FILE_LOCATION, false);
									StringBuilder msg = new StringBuilder()
									        .append("File ")
									        .append(entry.getName())
									        .append(" uploaded successfully");
									logToLens(msg.toString(), entry, ExecutionState.COMPLETED);
								}
							}
						}

					}

					if (failedStatus) {
						throw new MailBoxServicesException(Messages.HTTP_REQUEST_FAILED, Response.Status.BAD_REQUEST);
					}
				} else {
					LOGGER.info(constructMessage("The given HTTP uploader payload URI is Empty."));
				}
			}

			// to calculate the elapsed time for processing files
			long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));
		} catch (JAXBException | IOException | LiaisonException | IllegalAccessException | NoSuchFieldException e) {
		    LOGGER.error(constructMessage("Error occurred during http(s) upload", seperator, e.getMessage()), e);
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
	 * @throws JAXBException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	private void delegateArchiveFile(File file, String locationName, boolean isError) throws IOException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JAXBException {

		HTTPUploaderPropertiesDTO httpUploaderStaticProperties = (HTTPUploaderPropertiesDTO) getProperties();
		String filePath = (locationName.equals(MailBoxConstants.PROPERTY_ERROR_FILE_LOCATION)) ? httpUploaderStaticProperties
				.getErrorFileLocation() : httpUploaderStaticProperties.getProcessedFileLocation();
		String fileLocation = replaceTokensInFolderPath(filePath);

		if (MailBoxUtil.isEmpty(fileLocation)) {
			archiveFile(file.getAbsolutePath(), isError);
		} else {
			archiveFile(file, fileLocation);
		}
	}

	@Override
	public void runProcessor(Object dto, MailboxFSM fsm) {

		try {

		    setReqDTO((TriggerProcessorRequestDTO) dto);
			// HTTPRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// HTTPRequest executed through Java
				executeRequest(getReqDTO().getExecutionId(), fsm);
			}

		} catch (JAXBException | IOException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
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

	@Override
	public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargetLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	/**
	 * This Method create local folders if not available.
	 *
	 * * @param processorDTO it have details of processor
	 *
	 */
	@Override
	public void createLocalPath() {

		String configuredPath = null;
		try {
			configuredPath = getPayloadURI();
			createPathIfNotAvailable(configuredPath);

		} catch (IOException e) {
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED, configuredPath,
					Response.Status.BAD_REQUEST, e.getMessage());
		}

	}

   @Override
    public void logToLens(String msg, File file, ExecutionState status) {
        logGlassMessage(msg, file, status);
    }
}