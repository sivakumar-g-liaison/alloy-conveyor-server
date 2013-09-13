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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.framework.fs2.api.FS2Exception;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.HttpRemoteDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public class HttpRemoteDownloader extends MailBoxHandler implements	MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(HttpRemoteDownloader.class);

	// private Processor configurationInstance;

	@SuppressWarnings("unused")
	private HttpRemoteDownloader() {
		// to force creation of instance only by passing the processor entity
	}

	public HttpRemoteDownloader(Processor processor) {
		super(processor);
		// this.configurationInstance = processor;
	}

	/**
	 * Get HTTPRequest with injected configurations.
	 * 
	 * @return configured HTTPRequest
	 * @throws MailBoxServicesException 
	 */
	private HTTPRequest getClientWithInjectedConfiguration() throws MailBoxServicesException {

			LOGGER.info("Started injecting HTTP/S configurations to HTTPClient");
			// Create HTTPRequest and set the properties
			 HTTPRequest request = new HTTPRequest(null, LOGGER);
			
			try{
			// Convert the json string to DTO
			HttpRemoteDownloaderPropertiesDTO properties = MailBoxUtility.unmarshalFromJSON(configurationInstance.getProcsrProperties(),
					                                                                        HttpRemoteDownloaderPropertiesDTO.class);

			// Set url to HTTPRequest
			URL url = new URL(properties.getUrl());
			request.setUrl(url);

			// Set configurations
			request.setVersion(properties.getHttpVersion());
			request.setMethod(properties.getHttpVerb());
			request.setNumberOfRetries(properties.getRetryAttempts());
			request.setSocketTimeout(properties.getSocketTimeout());
			request.setConnectionTimeout(properties.getConnectionTimeout());
			request.setPort(properties.getPort());
			request.setChunkedEncoding(properties.isChunkedEncoding());

			// Set the Other header to HttpRequest
			if (properties.getOtherRequestHeader() != null) {
				for (HttpOtherRequestHeaderDTO header : properties
						.getOtherRequestHeader()) {
					request.addHeader(header.getName(), header.getValue());
				}
			}

			// Set the content type header to HttpRequest
			if (MailBoxUtility.isEmpty(properties.getContentType())) {
				request.addHeader("Content-Type", properties.getContentType());
			}

			LOGGER.info("Returns HTTP/S configured HTTPClient");
			
			}catch(Exception e){
				LOGGER.error("Injection of properties to HTTP Client failed",e);
				throw new MailBoxServicesException(Messages.INJECTION_OF_PROPERTIES_FAILED);
			}
			return request;

		} 		
	

	/**
	 * Java method to execute the HTTPRequest and write in FS location
	 * @throws MailBoxServicesException 
	 * @throws FS2Exception 
	 * @throws IOException 
	 * @throws LiaisonException 
	 * @throws URISyntaxException 
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	public void executeRequest() throws MailBoxServicesException, LiaisonException, IOException, FS2Exception, URISyntaxException  {

		HTTPRequest request = getClientWithInjectedConfiguration();
		ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
		request.setOutputStream(responseStream);

		// Set the pay load value to http client input data for POST & PUT request
		if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {
			// TODO read pay load value from file if exist
		}
        
		HTTPResponse response = request.execute();
		if (response.getStatusCode() != 200) {
			LOGGER.info("The reponse code recived is {} ",response.getStatusCode());
			throw new MailBoxServicesException(Messages.HTTP_REQUEST_FAILED);	
				} 
		writeResponseToMailBox(responseStream);
	}

	@Override
	public void invoke() {

		try {

			    // HTTPRequest executed through JavaScript
			if (configurationInstance.getJavaScriptUri() != null) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");
				//TODO actually read the JS from the URI stored i mean from the path returned in   getJavaScriptUri()
				String groupingConfiguration = ServiceUtils	.readFileFromClassPath("HTTPRemoteDataDownloader.js");
				engine.eval(groupingConfiguration);
				Invocable inv = (Invocable) engine;
				inv.invokeFunction("handleHttpRequest", this);
                
			} else {
				// HTTPRequest executed through Java
				executeRequest();
			}

		} catch (Exception e) {
			e.printStackTrace();
			//TODO Re stage and update status in FSM
		}

	}

}
