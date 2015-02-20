/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkResult;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.util.WorkTicketUtil;
import com.liaison.usermanagement.service.client.UserManagementClient;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.CompositeMonitor;
import com.netflix.servo.monitor.Monitors;

/**
 * G2 HTTP Gateway.
 *
 * @author OFS
 */

@Path("process")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class HttpListener extends AuditedResource {

	private static final Logger logger = LogManager.getLogger(HttpListener.class);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	protected static final String HTTP_METHOD_POST = "POST";

	protected static final String CONFIGURATION_SERVICE_BROKER_URI = "com.liaison.servicebroker.sync.uri";
	protected static final String CONFIGURATION_MAX_REQUEST_SIZE = "com.liaison.servicebroker.sync.max.request.size";
	protected static final String CONFIGURATION_QUEUE_PROVIDER_URL = "g2.queueing.server.url";
	protected static final String CONFIGURATION_QUEUE_NAME = "directory.sweeper.queue.name";
	protected static final String HTTP_HEADER_BASIC_AUTH = "Authorization";
	protected static final String GATEWAY_HEADER_PREFIX = "x-gate-";
	protected static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	protected static final String HTTP_HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
	protected static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

	private static final String AUTHENTICATION_HEADER_PREFIX = "Basic ";

	public HttpListener() {
		CompositeMonitor<?> monitor = Monitors.newObjectMonitor(this);
		MonitorRegistry monitorRegistry = DefaultMonitorRegistry.getInstance();
		monitorRegistry.register(monitor);
	}

	@POST
	@Path("sync/{token1}")
	public Response handleSyncOneToken(@Context HttpServletRequest request,
									   @Context HttpServletResponse response,
									   @QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleSync(request, mailboxPguid);
	}

	@POST
	@Path("sync/{token1}/{token2}")
	public Response handleSyncTwoTokens(@Context HttpServletRequest request,
										@Context HttpServletResponse response,
										@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleSync(request, mailboxPguid);
	}

	@POST
	@Path("sync/{token1}/{token2}/{token3}")
	public Response handleSyncThreeTokens(@Context HttpServletRequest request,
										  @Context HttpServletResponse response,
										  @QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleSync(request, mailboxPguid);
	}

	/**
	 * This method will processing the sync message by give request.
	 *
	 * @param request
	 *            The HttpServletRequest
	 * @return The Response Object
	 */
	@POST
	@Path("sync")
	@AccessDescriptor(skipFilter=true)
	public Response handleSync(@Context final HttpServletRequest request,
							   @QueryParam(value = "mailboxId") final String mailboxPguid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

				serviceCallCounter.incrementAndGet();

				logger.debug("Starting sync processing");
				try {
					validateRequestSize(request);
					if(StringUtils.isEmpty(mailboxPguid)){
						throw new RuntimeException(	"Mailbox ID is not passed as a query param (mailboxId) ");
					}

					Map <String,  String> httpListenerProperties = retrieveHttpListenerProperties(mailboxPguid, ProcessorType.HTTPSYNCPROCESSOR);
					// authentication should happen only if the property
					// "Http Listner Auth Check Required" is true
					logger.info("Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}", mailboxPguid);
					if (isAuthenticationCheckRequired(httpListenerProperties)) {
						authenticateRequestor(request);
					}
					logger.debug("constructed workticket");
					WorkTicket workTicket  = createWorkTicket(request, mailboxPguid, httpListenerProperties);

					HttpResponse httpResponse = forwardRequest(workTicket, request, httpListenerProperties);

					//GLASS LOGGING BEGINS//
					TransactionVisibilityClient glassLogger = new TransactionVisibilityClient(MailBoxUtil.getGUID());
					GlassMessage glassMessage = new GlassMessage();
					glassMessage.setCategory(ProcessorType.HTTPSYNCPROCESSOR);
					glassMessage.setProtocol(Protocol.HTTPSYNCPROCESSOR.getCode());
					glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
					glassMessage.setMailboxId(mailboxPguid);
					glassMessage.setStatus(ExecutionState.STAGED);
					glassMessage.setPipelineId(workTicket.getPipelineId());
					glassMessage.setInAgent(GatewayType.REST);
					glassLogger.logToGlass(glassMessage);
					//GLASS LOGGING ENDS//

					ResponseBuilder builder = Response.ok();
					copyResponseInfo(request, httpResponse, builder);

					return builder.build();
				} catch (IOException | JAXBException e) {
					logger.error(e.getMessage(), e);
					//throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
					throw new LiaisonRuntimeException(Messages.COMMON_SYNC_ERROR_MESSAGE.value());
				}

			}
		};
		worker.actionLabel = "HttpListener.handleSync()";
		worker.queryParams.put("guid", mailboxPguid);
		// hand the delegate to the framework for calling
		try {
			return handleAuditedServiceRequest(request, worker);
		} catch (LiaisonAuditableRuntimeException e) {
			if (!StringUtils.isEmpty(e.getResponseStatus().getStatusCode() + "")) {
				return marshalResponse(e.getResponseStatus().getStatusCode(), MediaType.TEXT_PLAIN, e.getMessage());
			}
			return marshalResponse(500, MediaType.TEXT_PLAIN, e.getMessage());
		}
	}

	@POST
	@Path("async/{token1}")
	public Response handleAsyncOneToken(@Context HttpServletRequest request,
										@Context HttpServletResponse response,
										@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleAsync(request, mailboxPguid);
	}

	@POST
	@Path("async/{token1}/{token2}")
	public Response handleAsyncTwoTokens(@Context HttpServletRequest request,
										 @Context HttpServletResponse response,
										 @QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleAsync(request, mailboxPguid);
	}

	@POST
	@Path("async/{token1}/{token2}/{token3}")
	public Response handleAsyncThreeTokens(@Context HttpServletRequest request,
										   @Context HttpServletResponse response,
										   @QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleAsync(request, mailboxPguid);
	}

	/**
	 * This method will processing the async message by give request.
	 *
	 * @param request
	 *            The HttpServletRequest
	 * @return The Response Object
	 */
	@POST
	@Path("async")
	@AccessDescriptor(skipFilter=true)
	public Response handleAsync(@Context final HttpServletRequest request,
								@QueryParam(value = "mailboxId") final String mailboxPguid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

				serviceCallCounter.incrementAndGet();

				logger.debug("Starting async processing");
				try {
					validateRequestSize(request);

					if(StringUtils.isEmpty(mailboxPguid)){
						throw new RuntimeException(	"Mailbox ID is not passed as a query param (mailboxId) ");
					}

					Map <String,  String> httpListenerProperties = retrieveHttpListenerProperties(mailboxPguid, ProcessorType.HTTPASYNCPROCESSOR);
					// authentication should happen only if the property
					// "Http Listner Auth Check Required" is true
					if (isAuthenticationCheckRequired(httpListenerProperties)) {
						authenticateRequestor(request);
					}
					WorkTicket workTicket = createWorkTicket(request, mailboxPguid, httpListenerProperties);
					StorageUtilities.storePayload(request.getInputStream(), workTicket, httpListenerProperties, false);
					workTicket.setProcessMode(ProcessMode.ASYNC);
					WorkTicketUtil.constructMetaDataJson(workTicket);

					//GLASS LOGGING BEGINS//
					TransactionVisibilityClient glassLogger = new TransactionVisibilityClient(MailBoxUtil.getGUID());
					GlassMessage glassMessage = new GlassMessage();
					glassMessage.setCategory(ProcessorType.HTTPASYNCPROCESSOR);
					glassMessage.setProtocol(Protocol.HTTPASYNCPROCESSOR.getCode());
					glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
					glassMessage.setMailboxId(mailboxPguid);
					glassMessage.setStatus(ExecutionState.STAGED);
					glassMessage.setPipelineId(workTicket.getPipelineId());
					glassMessage.setInAgent(GatewayType.REST);
					glassLogger.logToGlass(glassMessage);
					//GLASS LOGGING ENDS//

					return Response
							.ok()
							.status(Status.ACCEPTED)
							.type(MediaType.TEXT_PLAIN)
							.entity(String.format(
									"Payload accepted as process ID '%s'",
									workTicket.getGlobalProcessId())).build();
				} catch (IOException | JAXBException e) {
					logger.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "HttpListener.handleAsync()";

		// hand the delegate to the framework for calling
		try {
			return handleAuditedServiceRequest(request, worker);
		} catch (LiaisonAuditableRuntimeException e) {
			if (!StringUtils.isEmpty(e.getResponseStatus().getStatusCode() + "")) {
				return marshalResponse(e.getResponseStatus().getStatusCode(), MediaType.TEXT_PLAIN, e.getMessage());
			}
			return marshalResponse(500, MediaType.TEXT_PLAIN, e.getMessage());
		}
	}

	/**
	 * This method will validate the size of the request.
	 *
	 * @param request
	 *            The HttpServletRequest
	 */
	protected void validateRequestSize(HttpServletRequest request) {
		long contentLength = request.getContentLength();
		DecryptableConfiguration config = LiaisonConfigurationFactory
				.getConfiguration();
		int maxRequestSize = config.getInt(CONFIGURATION_MAX_REQUEST_SIZE);

		if (contentLength > maxRequestSize) {
			throw new RuntimeException("Request has content length of "
					+ contentLength
					+ " which exceeds the configured maximum size of "
					+ maxRequestSize);
		}
	}

	protected void authenticateRequestor(HttpServletRequest request) {

		// retrieving the authentication header from request
		String basicAuthenticationHeader = request
				.getHeader(HTTP_HEADER_BASIC_AUTH);
		if (!MailBoxUtil.isEmpty(basicAuthenticationHeader)) {

			// trim the prefix basic and get the username:password part
			basicAuthenticationHeader = basicAuthenticationHeader.replaceFirst(
					AUTHENTICATION_HEADER_PREFIX, "");
			// decode the string to get username and password
			String authenticationDetails = new String(
					Base64.decodeBase64(basicAuthenticationHeader));
			String[] authenticationCredentials = authenticationDetails
					.split(":");

			if (authenticationCredentials.length == 2) {

				String loginId = authenticationCredentials[0];
				// encode the password using base64 bcoz UM will expect a base64
				// encoded token
				String token = new String(
						Base64.encodeBase64(authenticationCredentials[1]
								.getBytes()));
				// if both username and password is present call UM client to
				// authenticate
				UserManagementClient UMClient = new UserManagementClient();
				UMClient.addAccount(UserManagementClient.TYPE_NAME_PASSWORD,
						loginId, token);
				UMClient.authenticate();
				if (!UMClient.isSuccessful()) {
					throw new RuntimeException(UMClient.getMessage());
				}
			} else {
				throw new RuntimeException(
						"Authorization Header does not contain UserName and Password");
			}
		} else {
			throw new RuntimeException(
					"Authorization Header not available in the Request");
		}

	}

	/**
	 * This method will create workTicket by given request.
	 *
	 * @param request
	 * @param mailboxPguid
	 * @param httpListenerProperties
	 * @return WorkTicket
	 */
/*	protected WorkTicket createWorkTicket(HttpServletRequest request, String mailboxPguid, Map <String, String> httpListenerProperties) {
		WorkTicket workTicket = new WorkTicket();
		workTicket.setAdditionalContext("httpMethod", request.getMethod());
		workTicket.setAdditionalContext("httpQueryString", request.getQueryString());
		workTicket.setAdditionalContext("httpRemotePort", request.getRemotePort());
		workTicket.setAdditionalContext("httpCharacterEncoding", (request.getCharacterEncoding() != null ? request.getCharacterEncoding() : ""));
		workTicket.setAdditionalContext("httpRemoteUser", (request.getRemoteUser() != null ? request.getRemoteUser() : "unknown-user"));
		workTicket.setAdditionalContext("mailboxId", mailboxPguid);
		workTicket.setAdditionalContext("httpRemoteAddress", request.getRemoteAddr());
		workTicket.setAdditionalContext("httpRequestPath", request.getRequestURL().toString());
		workTicket.setAdditionalContext("httpContentType", request.getContentType());
		workTicket.setPipelineId(retrievePipelineId(httpListenerProperties));
		copyRequestHeadersToWorkTicket(request, workTicket);
		assignGlobalProcessId(workTicket);
		assignTimestamp(workTicket);

		return workTicket;
	}

	/**
	 * Copies all the request header from HttpServletRequest to WorkTicket.
	 *
	 * @param request
	 *        HttpServletRequest
	 * @param request
	 *        workTicket
	 *
	 */
	/*protected void copyRequestHeadersToWorkTicket (HttpServletRequest request , WorkTicket workTicket)	{

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement();
			List<String> headerValues = new ArrayList<>();
			Enumeration<String> values = request.getHeaders(headerName);

			while (values.hasMoreElements())
			{
				headerValues.add(values.nextElement());
			}

			workTicket.addHeaders(headerName,  headerValues);
		}

	}

	/**
	 * This method will set globalProcessId to workTicket.
	 *
	 * @param workTicket
	 */
	/*protected void assignGlobalProcessId(WorkTicket workTicket) {
		UUIDGen uuidGen = new UUIDGen();
		String uuid = uuidGen.getUUID();
		workTicket.setGlobalProcessId(uuid);
	}

	protected void assignTimestamp(WorkTicket workTicket) {
		workTicket.setCreatedTime(new Date());
	}

	/**
	 * This method will persist payload in spectrum.
	 *
	 * @param request
	 * @param workTicket
	 * @throws IOException
	 */
	/*protected void storePayload(HttpServletRequest request,
			WorkTicket workTicket, Map <String, String> httpListenerProperties) throws Exception {

	  try (InputStream payloadToPersist = request.getInputStream()) {

              FS2ObjectHeaders fs2Header = constructFS2Headers(workTicket, httpListenerProperties);
              PayloadDetail payloadDetail = StorageUtilities.persistPayload(payloadToPersist, workTicket.getGlobalProcessId(),
                            fs2Header, Boolean.valueOf(httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_SECUREDPAYLOAD)));
              logger.info("The received path uri is {} ", (payloadDetail.getMetaSnapshot().getURI().toString()));

              workTicket.setPayloadSize(payloadDetail.getPayloadSize());
              workTicket.setPayloadURI(payloadDetail.getMetaSnapshot().getURI().toString());
	    }
	}

	protected void constructMetaDataJson(HttpServletRequest request,
			WorkTicket workTicket) throws Exception {
		String workTicketJson = JAXBUtility.marshalToJSON(workTicket);
		postToQueue(workTicketJson);
	}

	protected void postToQueue(String message) throws Exception {
        SweeperQueue.getInstance().sendMessages(message);
        logger.debug("HttpListener postToQueue, message: {}", message);

	}*/

	/**
	 * This method will persist payload in spectrum.
	 *
	 * @param sessionContext
	 * @param request
	 * @return HttpResponse
	 * @throws Exception
	 * @throws JAXBException
	 */
	protected HttpResponse forwardRequest(WorkTicket workTicket, HttpServletRequest request, Map <String, String> httpListenerProperties)
			throws JAXBException, Exception {
		logger.info("Starting to forward request...");

		//persist payload in spectrum
		StorageUtilities.storePayload(request.getInputStream(), workTicket, httpListenerProperties, false);
		workTicket.setProcessMode(ProcessMode.SYNC);
		String workTicketJson = JAXBUtility.marshalToJSON(workTicket);
		HttpPost httpRequest = createHttpRequest(request);
		httpRequest.setHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType());
		StringEntity requestBody =new StringEntity(workTicketJson);
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
	protected HttpClient createHttpClient() {
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
	protected HttpPost createHttpRequest(HttpServletRequest request) throws JAXBException, Exception {
		String serviceBrokerSyncUri = getServiceBrokerUriFromConfig();
		logger.info("Forward request to:"+serviceBrokerSyncUri);
		HttpPost post = new HttpPost(serviceBrokerSyncUri);
		return post;
	}

	/**
	 * This method will copy all Response Information.
	 *
	 * @param httpResponse
	 * @param builder
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws JAXBException
	 */
	protected void copyResponseInfo(HttpServletRequest request, HttpResponse httpResponse,
			ResponseBuilder builder) throws IllegalStateException, IOException, JAXBException {

		if (httpResponse.getStatusLine().getStatusCode() > 299) {

			WorkResult result = JAXBUtility.unmarshalFromJSON(httpResponse.getEntity().getContent(), WorkResult.class);
			builder.status(result.getStatus());

			//Sets the headers
			Set<String> headers = result.getHeaderNames();
			for (String name : headers) {
				builder.header(name, result.getHeader(name));
			}

			//If payload URI avail, reads payload from spectrum. Mostly it would be an error message payload
			if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {
				InputStream responseInputStream = StorageUtilities.retrievePayload(result.getPayloadURI());
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

			//Content type
			String contentType = result.getHeader(HTTP_HEADER_CONTENT_TYPE);
			if (contentType == null) {
				builder.header(HTTP_HEADER_CONTENT_TYPE, request.getContentType());
			}

	   	} else {

			InputStream responseInputStream = httpResponse.getEntity().getContent();
			WorkResult result = JAXBUtility.unmarshalFromJSON(responseInputStream, WorkResult.class);

			//sets status code from work result
			builder.status(result.getStatus());

			//Sets the headers
			Set<String> headers = result.getHeaderNames();
			for (String name : headers) {
				builder.header(name, result.getHeader(name));
			}

			//reads paylaod from spectrum
			if (!MailBoxUtil.isEmpty(result.getPayloadURI())) {
			   responseInputStream = StorageUtilities.retrievePayload(result.getPayloadURI());
			   if (responseInputStream != null) {
			       builder.entity(responseInputStream);
			   }
			}

		   //Content type
		   String contentType = result.getHeader(HTTP_HEADER_CONTENT_TYPE);
		   if (contentType == null) {
		       builder.header(HTTP_HEADER_CONTENT_TYPE, request.getContentType());
		   }

	   	}

	}

	/**
	 * This method will retrieve Response Content Length by given HttpResponse.
	 *
	 * @param httpResponse
	 * @return responsecontentLength
	 */
	protected int getResponseContentLength(HttpResponse httpResponse) {
		Header header = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_LENGTH);

		if (header == null) {
			// TODO - this should be an error.
			return 0;
		}

		String strLength = header.getValue();
		int length = Integer.parseInt(strLength);

		return length;
	}

	/**
	 * This method will retrieve Response Content type by given httpresponse.
	 *
	 * @param httpResponse
	 * @return ContentType
	 */
	protected ContentType getResponseContentType(HttpResponse httpResponse) {
		Header header = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_TYPE);

		if (header == null) {
			// TODO - this should be an error.
			return null;
		}

		String strContentType = header.getValue();
		ContentType contentType = ContentType.parse(strContentType);

		return contentType;
	}

	/**
	 * This method will retrieve Response headers by given httpresponse.
	 *
	 * @param httpResponse
	 * @param builder
	 */
	protected void copyResponseHeaders(HttpResponse httpResponse,
			ResponseBuilder builder) {
		// Copy headers that start with the Gateway prefix.
		Header[] headers = httpResponse.getAllHeaders();

		for (Header header : headers) {
			if (header.getName().regionMatches(true, 0, GATEWAY_HEADER_PREFIX,
					0, GATEWAY_HEADER_PREFIX.length())) {
				String name = header.getName();
				name = name.substring(GATEWAY_HEADER_PREFIX.length());
				builder.header(name, header.getValue());
			}
		}
	}

	/**
	 * This method will retrieve the Service Broker Uri from config.
	 *
	 * @return serviceBrokerUri
	 */
	protected String getServiceBrokerUriFromConfig() {
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
	 * Method to retrieve the mailbox from the pguid and return the value of
	 * HTTPListener propery "Http Listner Auth Check Required "
	 *
	 * @param mailboxpguid
	 * @return
	 * @throws Exception
	 */
	protected boolean isAuthenticationCheckRequired(Map <String, String> httpListenerProperties) {

		boolean isAuthCheckRequired = true;
		isAuthCheckRequired = Boolean.parseBoolean(httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_AUTH_CHECK));
		logger.info("Property httplistenerauthcheckrequired is configured in the mailbox and set to be {}", httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_AUTH_CHECK));
		return isAuthCheckRequired;
	}

	/**
	 * Method to retrieve http listener properties of processor of specific type by given mailboxGuid
	 *
	 * @param mailboxGuid mailbox Pguid
	 * @param isSync boolean specifying
	 * @return
	 */
	private Map <String, String> retrieveHttpListenerProperties(String mailboxGuid, ProcessorType processorType) {

		logger.info("retrieving the properties configured in httplistener of mailbox {}", mailboxGuid);
		ProcessorConfigurationService procsrService = new ProcessorConfigurationService();
		return procsrService.getHttpListenerProperties(mailboxGuid, processorType);

	}

	/**
	 * retrieve the pipeline id configured in httplistener of mailbox
	 *
	 * @param mailboxpguid
	 * @Param isSync boolean
	 * @return String pipeline id
	 *
	 *
	 */
	/*private String retrievePipelineId(Map <String, String> httpListenerProperties) {

		String pipelineId = null;
		pipelineId = httpListenerProperties.get(MailBoxConstants.HTTPLISTENER_PIPELINEID);
		logger.info("PIPELINE ID is set to be :"+pipelineId);
		return pipelineId;
	}

	/**
	 * Method to construct FS2ObjectHeaders from the given workTicket
	 *
	 * @param workTicket
	 * @return FS2ObjectHeaders
	 * @throws IOException
	 * @throws MailBoxServicesException
	 */
	/*private FS2ObjectHeaders constructFS2Headers(WorkTicket workTicket, Map <String, String> httpListenerProperties) {

		FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
		fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());
		fs2Header.addHeader(MailBoxConstants.KEY_PIPELINE_ID, workTicket.getPipelineId());
		fs2Header.addHeader(MailBoxConstants.KEY_SERVICE_INSTANCE_ID, httpListenerProperties.get(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
		fs2Header.addHeader(MailBoxConstants.KEY_TENANCY_KEY, (MailBoxConstants.PIPELINE_FULLY_QUALIFIED_PACKAGE + ":" + workTicket.getPipelineId()));
		logger.debug("FS2 Headers set are {}", fs2Header.getHeaders());
		return fs2Header;
	}*/


	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		return new DefaultAuditStatement(AuditStatement.Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
				PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
	}

	@Override
	protected void beginMetricsCollection() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endMetricsCollection(boolean success) {
		// TODO Auto-generated method stub

	}

}
