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
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.processor.helper.ClientFactory;
import com.liaison.mailbox.service.dto.configuration.TriggerProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPDownloaderPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Http remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class HTTPRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(HTTPRemoteDownloader.class);

	@SuppressWarnings("unused")
	private HTTPRemoteDownloader() {
		// to force creation of instance only by passing the processor entity
	}

	public HTTPRemoteDownloader(Processor processor) {
		super(processor);
		// this.configurationInstance = processor;
	}

	@Override
	public void runProcessor(TriggerProcessorRequestDTO dto, MailboxFSM fsm) {

		LOGGER.debug("Entering in invoke.");

		try {
			// HTTPRequest executed through JavaScript
			if (getProperties().isHandOverExecutionToJavaScript()) {
				fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
				// Use custom G2JavascriptEngine
				JavaScriptExecutorUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), this);

			} else {
				// HTTPRequest executed through Java
				executeRequest();
			}

		} catch (JAXBException | IOException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
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
	 *
	 * @throws MailBoxConfigurationServicesException
	 * @throws SymmetricAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws com.liaison.commons.exception.LiaisonException
	 * @throws BootstrapingFailedException
	 * @throws CMSException
	 * @throws OperatorCreationException
	 * @throws UnrecoverableKeyException
	 *
	 */
	protected void executeRequest() {

		HTTPRequest request = (HTTPRequest) getClient();
		ByteArrayOutputStream responseStream = new ByteArrayOutputStream(4096);
		request.setOutputStream(responseStream);

		HTTPResponse response = null;
		boolean failedStatus = false;
		long startTime = 0;
		// Set the pay load value to http client input data for POST & PUT
		// request
		File[] files = null;
		try {

			LOGGER.info(constructMessage("Start run"));
			startTime = System.currentTimeMillis();
			if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {

				HTTPDownloaderPropertiesDTO httpDownloaderStaticProperties = (HTTPDownloaderPropertiesDTO) getProperties();

				files = getFilesToUpload();
				if (null != files) {

					for (File entry : files) {

						try (InputStream contentStream = FileUtils.openInputStream(entry)) {

							String contentType = httpDownloaderStaticProperties.getContentType();
							request.inputData(contentStream, contentType);

							response = request.execute();
							LOGGER.info("The reponse code received is {} for a request {} ", response.getStatusCode(),
									entry.getName());
							if (response.getStatusCode() != 200) {

								LOGGER.info("The reponse code received is {} ", response.getStatusCode());
								LOGGER.info("Execution failure for ", entry.getAbsolutePath());

								failedStatus = true;
								delegateArchiveFile(entry, MailBoxConstants.PROPERTY_ERROR_FILE_LOCATION, true);
								// continue;

							} else {

								if (null != entry) {
									delegateArchiveFile(entry, MailBoxConstants.PROPERTY_PROCESSED_FILE_LOCATION, false);
								}
								totalNumberOfProcessedFiles++;
							}
						}
					}
					if (failedStatus) {
						throw new MailBoxServicesException(Messages.HTTP_REQUEST_FAILED, Response.Status.BAD_REQUEST);
					}
				}
			} else {
				response = request.execute();
				writeResponseToMailBox(responseStream);
				totalNumberOfProcessedFiles++;
			}
			// to calculate the elapsed time for processing files
			long endTime = System.currentTimeMillis();
            LOGGER.info(constructMessage("Number of files processed {}"), totalNumberOfProcessedFiles);
            LOGGER.info(constructMessage("Total time taken to process files {}"), endTime - startTime);
            LOGGER.info(constructMessage("End run"));

		} catch (MailBoxServicesException | IOException | JAXBException | LiaisonException | URISyntaxException
				| IllegalAccessException | NoSuchFieldException e) {
		    LOGGER.error(constructMessage("Error occured during http(s) download"), e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public Object getClient() {
		return ClientFactory.getClient(this);
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
	 */
	@Override
	public void createLocalPath() {

		String configuredPath = null;
		try {
			configuredPath = getWriteResponseURI();
			createPathIfNotAvailable(configuredPath);

		} catch (IOException e) {
			throw new MailBoxConfigurationServicesException(Messages.LOCAL_FOLDERS_CREATION_FAILED, configuredPath,
					Response.Status.BAD_REQUEST, e.getMessage());
		}

	}
}
