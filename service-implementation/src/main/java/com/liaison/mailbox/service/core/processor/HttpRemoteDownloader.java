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
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.exceptions.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.framework.fs2.api.FS2Exception;
import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.dto.configuration.request.HttpOtherRequestHeaderDTO;
import com.liaison.mailbox.service.dto.configuration.request.HttpRemoteDownloaderPropertiesDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public class HttpRemoteDownloader extends MailBoxHandler implements MailBoxProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRemoteDownloader.class);

	// private Processor configurationInstance;

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
	 */
	@Override
	public HTTPRequest getClientWithInjectedConfiguration() {

		try {

			LOGGER.info("Started injecting HTTP/S configurations to HTTPClient");

			// Convert the json string to DTO
			HttpRemoteDownloaderPropertiesDTO properties = MailBoxUtility.unmarshalFromJSON(
					configurationInstance.getProcsrProperties(), HttpRemoteDownloaderPropertiesDTO.class);

			// Create HTTPRequest and set the properties
			HTTPRequest request = new HTTPRequest(null, LOGGER);

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
				for (HttpOtherRequestHeaderDTO header : properties.getOtherRequestHeader()) {
					request.addHeader(header.getName(), header.getValue());
				}
			}

			// Set the content type header to HttpRequest
			if (MailBoxUtility.isEmpty(properties.getContentType())) {
				request.addHeader("Content-Type", properties.getContentType());
			}

			LOGGER.info("Returns HTTP/S configured HTTPClient");
			return request;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Java method to execute the HTTPRequest and write in FS location
	 * 
	 * @throws MailBoxConfigurationServicesException
	 * 
	 */
	public void executeJavaHTTPRequest() throws JsonParseException, JsonMappingException, NumberFormatException, JAXBException,
			IOException, LiaisonException, FS2Exception, JSONException, MailBoxConfigurationServicesException {

		// HTTP/S request configuration
		HTTPRequest request = getClientWithInjectedConfiguration();
		ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
		request.setOutputStream(responseStream);

		// Set the payload value to httpclient inputdata for POST & PUT request
		if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {
			// TODO read payload value from file if exist
		}

		if (request.execute().getStatusCode() == 200) {
			// write the response to mailbox
			writeResponseToMailBox(responseStream);
		} else {
			LOGGER.debug(request.execute().getStatusCode() + responseStream.toString());
		}
	}

	@Override
	public void invoke() {

		// initiator
		try {

			// HTTPRequest executed through JavaScript
			if (configurationInstance.getJavaScriptUri() != null) {

				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");
				String groupingConfiguration = ServiceUtils.readFileFromClassPath("HTTPRemoteDataDownloader.js");
				engine.eval(groupingConfiguration);
				Invocable inv = (Invocable) engine;
				inv.invokeFunction("handleHttpRequest", this);

			} else {
				// HTTPRequest executed through Java
				executeJavaHTTPRequest();
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
