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
import com.liaison.mailbox.service.dto.configuration.processor.properties.ProcessorPropertiesDefinitionDTO;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.executor.javascript.JavaScriptExecutorUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;

/**
 * Http remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 *
 * @author OFS
 */
public class HttpRemoteDownloader extends AbstractProcessor implements MailBoxProcessorI {

	private static final Logger LOGGER = LogManager.getLogger(HttpRemoteDownloader.class);

	@SuppressWarnings("unused")
	private HttpRemoteDownloader() {
		// to force creation of instance only by passing the processor entity
	}

	public HttpRemoteDownloader(Processor processor) {
		super(processor);
		// this.configurationInstance = processor;
	}

	@Override
	public void invoke(String executionId,MailboxFSM fsm) {

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
			
		} catch(JAXBException |IOException |IllegalAccessException | NoSuchFieldException e) {			
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

		// Set the pay load value to http client input data for POST & PUT request
		File[] files = null;

		try {

			if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {

			    ProcessorPropertiesDefinitionDTO processorProperties = getProperties();

			    files = getFilesToUpload();
				if (null != files) {

					for (File entry : files) {

					    try (InputStream contentStream = FileUtils.openInputStream(entry)) {
					    	
					    	String contentType = ProcessorPropertyJsonMapper.getProcessorProperty(processorProperties, MailBoxConstants.PROPERTY_CONTENT_TYPE);
					        request.inputData(contentStream, contentType);

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
				}
			} else {
			    response = request.execute();
		        writeResponseToMailBox(responseStream);
			}

		} catch(MailBoxServicesException | IOException | JAXBException | LiaisonException 
				| URISyntaxException | IllegalAccessException | NoSuchFieldException e) {
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
	public void downloadDirectory(Object client, String remotePayloadLocation, String localTargetLocation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadDirectory(Object client, String localPayloadLocation, String remoteTargetLocation ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
