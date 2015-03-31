package com.liaison.mailbox.service.core.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkResult;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.rest.HTTPListenerResource;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

public class HTTPProcessor {

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
	public HttpResponse forwardRequest(WorkTicket workTicket,
			HttpServletRequest request,
			Map<String, String> httpListenerProperties) throws JAXBException,
			Exception {
		logger.info("Starting to forward request...");

		// persist payload in spectrum
		StorageUtilities.storePayload(request.getInputStream(), workTicket,
				httpListenerProperties, false);
		workTicket.setProcessMode(ProcessMode.SYNC);
		String workTicketJson = JAXBUtility.marshalToJSON(workTicket);
		HttpPost httpRequest = createHttpRequest(request);
		httpRequest.setHeader("Content-type",
				ContentType.APPLICATION_JSON.getMimeType());
		StringEntity requestBody = new StringEntity(workTicketJson);
		httpRequest.setEntity(requestBody);
		HttpClient httpClient = createHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpRequest);
		return httpResponse;
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
	 * This method will create HttpPost request by given request.
	 * 
	 * @return HttpPost
	 * @throws Exception
	 * @throws JAXBException
	 */
	private HttpPost createHttpRequest(HttpServletRequest request)
			throws JAXBException, Exception {
		String serviceBrokerSyncUri = getServiceBrokerUriFromConfig();
		logger.info("Forward request to:" + serviceBrokerSyncUri);
		HttpPost post = new HttpPost(serviceBrokerSyncUri);
		return post;
	}

	/**
	 * This method will retrieve the Service Broker Uri from config.
	 * 
	 * @return serviceBrokerUri
	 */
	private String getServiceBrokerUriFromConfig() {
		DecryptableConfiguration config = LiaisonConfigurationFactory
				.getConfiguration();
		String serviceBrokerUri = config
				.getString(CONFIGURATION_SERVICE_BROKER_URI);

		if ((serviceBrokerUri == null)
				|| (serviceBrokerUri.trim().length() == 0)) {
			throw new RuntimeException("Service Broker URI not configured ('"
					+ CONFIGURATION_SERVICE_BROKER_URI
					+ "'), cannot process sync");
		}

		return serviceBrokerUri;
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
	public void copyResponseInfo(HttpServletRequest request,
			HttpResponse httpResponse, ResponseBuilder builder)
			throws IllegalStateException, IOException, JAXBException {

		if (httpResponse.getStatusLine().getStatusCode() > 299) {
			logger.debug(
					"THE RESPONSE RECEIVED FROM SERVICE BROKER IS:FAILED. Actual:{}",
					httpResponse.getEntity().getContent());
			WorkResult result = JAXBUtility.unmarshalFromJSON(httpResponse
					.getEntity().getContent(), WorkResult.class);
			builder.status(result.getStatus());

			// Sets the headers
			Set<String> headers = result.getHeaderNames();

			for (String name : headers) {
				builder.header(name, result.getHeader(name));
			}
			// set global process id in the header
			builder.header(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER,
					result.getProcessId());

			// If payload URI avail, reads payload from spectrum. Mostly it
			// would be an error message payload
			if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {
				InputStream responseInputStream = StorageUtilities
						.retrievePayload(result.getPayloadURI());
				if (responseInputStream != null) {
					builder.entity(responseInputStream);
				}
			} else {
				if (!MailBoxUtil.isEmpty(result.getErrorMessage())) {
					builder.entity(result.getErrorMessage());
				} else {
					builder.entity(Messages.COMMON_SYNC_ERROR_MESSAGE.value());
				}
			}

			// Content type
			String contentType = result
					.getHeader(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE);
			if (contentType == null) {
				builder.header(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE,
						request.getContentType());
			}

		} else {

			InputStream responseInputStream = httpResponse.getEntity()
					.getContent();
			WorkResult result = JAXBUtility.unmarshalFromJSON(
					responseInputStream, WorkResult.class);

			// sets status code from work result
			builder.status(result.getStatus());

			// Sets the headers
			Set<String> headers = result.getHeaderNames();
			for (String name : headers) {
				builder.header(name, result.getHeader(name));
			}
			// set global process id in the header
			builder.header(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER,
					result.getProcessId());

			// reads paylaod from spectrum
			if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {
				responseInputStream = StorageUtilities.retrievePayload(result
						.getPayloadURI());
				if (responseInputStream != null) {
					builder.entity(responseInputStream);
				}
			}

			// Content type
			String contentType = result
					.getHeader(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE);
			if (contentType == null) {
				builder.header(MailBoxConstants.HTTP_HEADER_CONTENT_TYPE,
						request.getContentType());
			}

		}

	}

}
