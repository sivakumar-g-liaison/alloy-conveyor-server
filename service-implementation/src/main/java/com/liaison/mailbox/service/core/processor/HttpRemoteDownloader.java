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
import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.dto.configuration.request.RemoteProcessorPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.JavaScriptEngineUtil;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Http remote downloader to perform pull operation, also it has support methods
 * for JavaScript.
 * 
 * @author OFS
 */
public class HttpRemoteDownloader extends AbstractRemoteProcessor implements MailBoxProcessor {

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
	public void invoke(String executionId,MailboxFSM fsm) throws Exception {

		LOGGER.debug("Entering in invoke.");
		// HTTPRequest executed through JavaScript
		if (!MailBoxUtil.isEmpty(configurationInstance.getJavaScriptUri())) {

			fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_HANDED_OVER_TO_JS));
			
			// Use custom G2JavascriptEngine
			JavaScriptEngineUtil.executeJavaScript(configurationInstance.getJavaScriptUri(), "init", this,LOGGER);

		} else {
			// HTTPRequest executed through Java
			executeRequest();
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
	protected void executeRequest() throws MailBoxServicesException, LiaisonException, IOException, FS2Exception,
			URISyntaxException, JAXBException, MailBoxConfigurationServicesException, SymmetricAlgorithmException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException, JsonParseException, JSONException, com.liaison.commons.exception.LiaisonException, UnrecoverableKeyException, OperatorCreationException, CMSException, BootstrapingFailedException {

		HTTPRequest request = (HTTPRequest) getClientWithInjectedConfiguration();
		ByteArrayOutputStream responseStream = new ByteArrayOutputStream(4096);
        request.setOutputStream(responseStream);
        
        HTTPResponse response = null;
        boolean failedStatus = false;
           
		// Set the pay load value to http client input data for POST & PUT
		// request
		File[] files = null;
		
		if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {
		    
		    RemoteProcessorPropertiesDTO remoteProcessorProperties = getRemoteProcessorProperties();

		    files = getProcessorPayload();
			if (null != files) {

				for (File entry : files) {
				    
				    try (InputStream contentStream = FileUtils.openInputStream(entry)) {
				        
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
			}
		} else {
		    response = request.execute();
	        writeResponseToMailBox(responseStream);
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

        String fileLocation = processMountLocation(getDynamicProperties().getProperty(locationName));
        if (MailBoxUtil.isEmpty(fileLocation)) {
            archiveFile(file.getAbsolutePath(), isError);
        } else {
            archiveFile(file, fileLocation);
        }
    }
}
