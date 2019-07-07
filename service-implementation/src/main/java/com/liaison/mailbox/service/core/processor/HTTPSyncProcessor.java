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

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkResult;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.rest.HTTPListenerResource;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static com.liaison.mailbox.MailBoxConstants.BYTE_ARRAY_INITIAL_SIZE;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_CONNECTION_TIMEOUT;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SERVICE_BROKER_URI;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SOCKET_TIMEOUT;
import static com.liaison.mailbox.MailBoxConstants.CONNECTION_TIMEOUT;
import static com.liaison.mailbox.MailBoxConstants.KEY_RAW_PAYLOAD_SIZE;
import static com.liaison.mailbox.MailBoxConstants.SOCKET_TIMEOUT;

/**
 * Class that deals with processing of sync request.
 * 
 * @author OFS
 */
public class HTTPSyncProcessor extends HTTPAbstractProcessor {

	private static final Logger logger = LogManager.getLogger(HTTPListenerResource.class);
	private static String SERVICE_BROKER_URI = null;
	private static int ENV_CONNECTION_TIMEOUT_VALUE = 0;
	private static int ENV_SOCKET_TIMEOUT_VALUE = 0;
    private static final String ERROR_MESSAGE = "HTTP Sync Request failed"; 

	private long payloadSize = 0;

	public long getPayloadSize() {
		return payloadSize;
	}

	public void setPayloadSize(long payloadSize) {
		this.payloadSize = payloadSize;
	}

	static {

		SERVICE_BROKER_URI = MailBoxUtil.getEnvironmentProperties().getString(CONFIGURATION_SERVICE_BROKER_URI);
		if (MailBoxUtil.isEmpty(SERVICE_BROKER_URI)) {
			throw new RuntimeException("Service Broker URI not configured ('" + CONFIGURATION_SERVICE_BROKER_URI + "'), cannot process sync");
		}

		ENV_CONNECTION_TIMEOUT_VALUE = MailBoxUtil.getEnvironmentProperties().getInt(CONFIGURATION_CONNECTION_TIMEOUT, 60000);
		ENV_SOCKET_TIMEOUT_VALUE = MailBoxUtil.getEnvironmentProperties().getInt(CONFIGURATION_SOCKET_TIMEOUT, 60000);
	}

	/**
	 * This method will persist payload in spectrum.
	 *
	 * @param workTicket workticket to be posted to SB
	 * @param httpListenerProperties props
	 * @param contentType request content type
     * @param glassMessage 
	 * @return response
     * @throws Exception
     */
	public Response processRequest(WorkTicket workTicket,
								   Map<String, String> httpListenerProperties,
                                   String contentType, GlassMessage glassMessage) throws Exception {

		logger.info("Starting to forward request to sb");

		workTicket.setProcessMode(ProcessMode.SYNC);
		try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_SIZE)) {

            HTTPRequest request = HTTPRequest.post(SERVICE_BROKER_URI)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
					.connectionTimeout(getTimeout(httpListenerProperties, CONNECTION_TIMEOUT, ENV_CONNECTION_TIMEOUT_VALUE))
					.socketTimeout(getTimeout(httpListenerProperties, SOCKET_TIMEOUT, ENV_SOCKET_TIMEOUT_VALUE))
					.inputData(JAXBUtility.marshalToJSON(workTicket))
					.outputStream(responseStream);

			// execute request and handle response
			HTTPResponse response = request.execute();
            return buildResponse(contentType, response, responseStream.toString(), glassMessage);

		}
	}

    /**
	 * This method will copy all Response Information from SB
	 *
	 * @param reqContentType request content type
	 * @param httpResponse sb response
     * @param glassMessage 
	 * @return response
	 * @throws IOException
     * @throws JAXBException
     */
    private Response buildResponse(String reqContentType, HTTPResponse httpResponse, String response, GlassMessage glassMessage) throws IOException, JAXBException {

		ResponseBuilder builder = Response.ok();

		// if the status code is set as 304 the response does not include the entity
		// this causes NPE. so added null check for httpEntity
		// if the status code is set as 205 the response must not include the entity
		// reference : https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
		// if entity is null we cannot process the sync response
        build(builder, response, reqContentType, httpResponse.getStatusCode(), glassMessage);
		return builder.build();
	}

	/**
	 * response builder for both success and error response
	 *
	 * @param builder response builder
	 * @param response response from SB
	 * @param reqContentType request content type
     * @param glassMessage 
	 * @throws IOException
	 * @throws JAXBException
     */
    private void build(ResponseBuilder builder, String response, String reqContentType, int status, GlassMessage glassMessage) throws IOException, JAXBException {

		if (MailBoxUtil.isEmpty(response)) {
			logger.warn("No response received from service broker");
			builder.status(status);
			return;
		}

		WorkResult result = null;
		try {
			result = JAXBUtility.unmarshalFromJSON(response, WorkResult.class);
		} catch (JsonParseException e) {
			logger.error("Failed to parse the service broker response", response);
			builder.status(status);
			builder.entity(response);
			return;
		}
		builder.status(result.getStatus());

        // Sets the headers
        Set<String> headers = result.getHeaderNames();
        for (String name : headers) {
			if (KEY_RAW_PAYLOAD_SIZE.equals(name)) {
				setPayloadSize(Long.valueOf(result.getHeader(name)));
				continue;
			}
			builder.header(name, result.getHeader(name));
		}

		// JIRA GMB-428 - set global process id in header only if it is not already available
		if (!headers.contains(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER)) {
            // set global process id in the header
            builder.header(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER, result.getProcessId());
        }

		// Content type
        if (result.getHeader(HttpHeaders.CONTENT_TYPE) == null
                && result.getHeader(HttpHeaders.CONTENT_TYPE.toLowerCase()) == null) {
            builder.header(MailBoxConstants.CONTENT_TYPE, reqContentType);
        }

		//sets the response payload for both success and error case
		if (MailBoxUtil.isSuccessful(result.getStatus())) {

			// reads payload from spectrum
			if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {
				InputStream responseInputStream = StorageUtilities.retrievePayload(result.getPayloadURI());
				if (responseInputStream != null) {
					builder.entity(responseInputStream);
				}
			}

		} else {

            // If payload URI avail, reads payload from spectrum. Mostly it
            // would be an error message payload
            if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {

                InputStream inputStream = null;
                try {
                    inputStream = StorageUtilities.retrievePayload(result.getPayloadURI());
                    if (inputStream != null) {
                        builder.entity(IOUtils.toString(inputStream, CharEncoding.UTF_8));
                    }
                } finally {
                    if (null != inputStream) {
                        inputStream.close();
                    }
                }
            } else if (!MailBoxUtil.isEmpty(result.getErrorMessage())) {
                builder.entity(result.getErrorMessage());
            } else {
                builder.entity(Messages.COMMON_SYNC_ERROR_MESSAGE.value());
            }

            if (!MailBoxUtil.isEmpty(result.getErrorMessage())) {
                glassMessage.logProcessingStatus(StatusType.ERROR, ERROR_MESSAGE,
                        MailBoxConstants.HTTPSYNCPROCESSOR, result.getErrorMessage());
            }
        }
    }

    /**
     * get timeout from the configuration
     * returns default if it is configured 0 or not configured
     *
     * @param httpListenerProperties properties
     * @return socket timeout
     */
    private int getTimeout(Map<String, String> httpListenerProperties, String propName, int defaultValue) {

        String timeoutStr = httpListenerProperties.get(propName);
        if (MailBoxUtil.isEmpty(timeoutStr)) {
            return defaultValue;
        }

        int timeout = Integer.parseInt(timeoutStr);
        if (timeout <= 0) {
            return defaultValue;
        }

        return timeout;
    }
}