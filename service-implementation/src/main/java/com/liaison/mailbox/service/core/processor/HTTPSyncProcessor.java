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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkResult;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.rest.HTTPListenerResource;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class that deals with processing of sync request.
 * 
 * @author OFS
 */
public class HTTPSyncProcessor extends HTTPAbstractProcessor {

	private static final Logger logger = LogManager.getLogger(HTTPListenerResource.class);
	private static final String CONFIGURATION_SERVICE_BROKER_URI = "com.liaison.servicebroker.sync.uri";

	/**
	 * This method will persist payload in spectrum.
	 *
	 * @param sessionContext
	 * @param request
	 * @return HttpResponse
	 * @throws Exception
	 * @throws JAXBException
	 */
	public Response processRequest(WorkTicket workTicket, InputStream inputStream,
			Map<String, String> httpListenerProperties, String contentType) throws Exception {
		logger.info("Starting to forward request...");

		workTicket.setProcessMode(ProcessMode.SYNC);

		String serviceBrokerSyncUri = MailBoxUtil.getEnvironmentProperties().getString(CONFIGURATION_SERVICE_BROKER_URI);
        if (serviceBrokerSyncUri.isEmpty()) {
			throw new RuntimeException("Service Broker URI not configured ('" + CONFIGURATION_SERVICE_BROKER_URI + "'), cannot process sync");
		}

		HttpPost httpRequest = new HttpPost(serviceBrokerSyncUri);
		httpRequest.setHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType());
		StringEntity requestBody = new StringEntity(JAXBUtility.marshalToJSON(workTicket));
		httpRequest.setEntity(requestBody);
		HttpClient httpClient = createHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpRequest);
		return buildResponse(contentType, httpResponse);

	}

	/**
	 * This method will create HttpClient.
	 *
	 * @return HttpClient
	 */
	private HttpClient createHttpClient() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		HttpClient httpClient = httpClientBuilder.build();
		return httpClient;
	}


	/**
	 * This method will copy all Response Information.
	 *
	 * @param httpResponse
	 * @param builder
	 * @param globalProcessId
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws JAXBException
	 */
	private Response buildResponse(String reqContentType, HttpResponse httpResponse)
			throws IllegalStateException, IOException, JAXBException {

		ResponseBuilder builder = Response.ok();
		
		// Fix for GMB-716, if the status code is set as 304 the response does not include the entity
		// this causes NPE. so added null check for httpEntity
		HttpEntity httpEntity = httpResponse.getEntity();
		if (httpResponse.getStatusLine().getStatusCode() > 299 && null != httpEntity) {
			logger.debug("THE RESPONSE RECEIVED FROM SERVICE BROKER IS:FAILED. Actual:{}", httpEntity
					.getContent());
			WorkResult result = JAXBUtility.unmarshalFromJSON(httpEntity.getContent(), WorkResult.class);
			builder.status(result.getStatus());

			// Sets the headers
			Set<String> headers = result.getHeaderNames();

			for (String name : headers) {
				builder.header(name, result.getHeader(name));
			}
			// JIRA GMB-428 - set global process id in header only if it is not already available
			if (!headers.contains(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER)) {
				// set global process id in the header
				builder.header(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER, result.getProcessId());
			}


			// If payload URI avail, reads payload from spectrum. Mostly it
			// would be an error message payload
			if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {
				InputStream responseInputStream = StorageUtilities.retrievePayload(result.getPayloadURI());
				if (responseInputStream != null) {
					builder.entity(IOUtils.toString(responseInputStream, CharEncoding.UTF_8));
				}
			} else {
				if (!MailBoxUtil.isEmpty(result.getErrorMessage())) {
					builder.entity(result.getErrorMessage());
				} else {
					builder.entity(Messages.COMMON_SYNC_ERROR_MESSAGE.value());
				}
			}

			// Content type
			String contentType = result.getHeader(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE);
			if (contentType == null) {
				builder.header(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE, reqContentType);
			}

		} else {
			
			// Fix for GMB-716, if the status code is set as 205 the response must not include the entity
			// reference : https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
			// if entity is null we cannot process the sync response 
			if (null != httpEntity) {
				
				InputStream responseInputStream = httpEntity.getContent();
				WorkResult result = JAXBUtility.unmarshalFromJSON(responseInputStream, WorkResult.class);
				logger.debug("THE RESPONSE RECEIVED FROM SERVICE BROKER IS: {}", JAXBUtility.marshalToJSON(result));
				// sets status code from work result
				builder.status(result.getStatus());

				// Sets the headers
				Set<String> headers = result.getHeaderNames();
				for (String name : headers) {
					builder.header(name, result.getHeader(name));
				}
				// JIRA GMB-428 - set global process id in header only if it is not already available
				if (!headers.contains(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER)) {
					// set global process id in the header
					builder.header(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER, result.getProcessId());
				}

				// reads paylaod from spectrum
				if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {
					responseInputStream = StorageUtilities.retrievePayload(result.getPayloadURI());
					if (responseInputStream != null) {
						builder.entity(responseInputStream);
					}
				}

				// Content type
				String contentType = result.getHeader(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE);
				if (contentType == null) {
					builder.header(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE, reqContentType);
				}
			}
		}
		return builder.build();
	}

}
