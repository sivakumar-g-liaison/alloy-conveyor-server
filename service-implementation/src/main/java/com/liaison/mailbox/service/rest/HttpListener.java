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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;
import com.liaison.commons.util.StreamUtil;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.com.liaison.queue.SweeperQueue;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.PropertyDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.SessionContext;
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
public class HttpListener extends BaseResource {

	private static final Logger logger = LogManager.getLogger(HttpListener.class);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	protected static final String HTTP_METHOD_POST = "POST";

	protected static final String CONFIGURATION_SERVICE_BROKER_URI = "com.liaison.servicebroker.sync.uri";
	protected static final String CONFIGURATION_MAX_REQUEST_SIZE = "com.liaison.servicebroker.sync.max.request.size";
	protected static final String CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR = "com.liaison.mailbox.http.async.payload.dir";
	protected static final String CONFIGURATION_QUEUE_PROVIDER_URL = "g2.queueing.server.url";
	protected static final String CONFIGURATION_QUEUE_NAME = "directory.sweeper.queue.name";
	protected static final String HTTP_HEADER_BASIC_AUTH = "Authorization";
	protected static final String GATEWAY_HEADER_PREFIX = "x-gate-";
	protected static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	protected static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

	private static final String AUTHENTICATION_HEADER_PREFIX = "Basic ";
	
	private static final String HTTP_LISTENER_AUTHENTICATION_CHECK = "httplistenerauthcheckrequired";
	private static final String HTTP_LISTENER_PIPELINEID = "httplistenerpipelineid";

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
	public Response handleSync(@Context HttpServletRequest request,
							   @QueryParam(value = "mailboxId") String mailboxPguid) {
		// Audit LOG the Attempt to handleSync
		auditAttempt("handleSync");

		Response restResponse = null;
		InputStream responseInputStream = null;
		serviceCallCounter.incrementAndGet();

		logger.debug("Starting sync processing");
		try {
			validateRequestSize(request);
			if(StringUtils.isEmpty(mailboxPguid)){
				throw new RuntimeException(	"Mailbox ID is not passed as a query param (mailboxId) ");
			}
			// authentication should happen only if the property
			// "Http Listner Auth Check Required" is true
			if (isAuthenticationCheckRequired(mailboxPguid)) {
				authenticateRequestor(request);
			}
			SessionContext sessionContext = createSessionContext(request,mailboxPguid);
			
			logger.debug("Pipeline id is set in session context");
			assignGlobalProcessId(sessionContext);
			assignTimestamp(sessionContext);

			HttpResponse httpResponse = forwardRequest(sessionContext, request);
			ResponseBuilder builder = Response.ok();
			responseInputStream = httpResponse.getEntity().getContent();
			copyResponseInfo(httpResponse, builder, responseInputStream);
			restResponse = builder.build();
			// Audit LOG the success
            auditSuccess("handleSync");
		} catch (Exception e) {
			logger.error("Error processing sync message", e);
			restResponse = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
			
			// Audit LOG the failure
			auditFailure("handleSync");
		} finally {
		   /* if (null != responseInputStream) {
		        try {
		            responseInputStream.close();
		        } catch (IOException e) {
		            logger.error("could not close response stream");
		        }
		       
		    }*/
		}
		return restResponse;
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
	public Response handleAsync(@Context HttpServletRequest request,
								@QueryParam(value = "mailboxId") String mailboxPguid) {
		// Audit LOG the Attempt to handleAsync
		auditAttempt("handleAsync");
		Response restResponse = null;
		serviceCallCounter.incrementAndGet();

		logger.debug("Starting async processing");
		try {
			validateRequestSize(request);
			
			if(StringUtils.isEmpty(mailboxPguid)){
				throw new RuntimeException(	"Mailbox ID is not passed as a query param (mailboxId) ");
			}
			// authentication should happen only if the property
			// "Http Listner Auth Check Required" is true
			if (isAuthenticationCheckRequired(mailboxPguid)) {
				authenticateRequestor(request);
			}
			SessionContext sessionContext = createSessionContext(request,mailboxPguid);			
			assignGlobalProcessId(sessionContext);
			assignTimestamp(sessionContext);

			storePayload(request, sessionContext);
			createWorkTicket(request, sessionContext);

			// Audit LOG the success
			auditSuccess("handleAsync");
			restResponse = Response
					.ok()
					.status(Status.ACCEPTED)
					.type(MediaType.TEXT_PLAIN)
					.entity(String.format(
							"Payload accepted as process ID '%s'",
							sessionContext.getGlobalProcessId())).build();
		} catch (Exception e) {
			logger.error("Error processing async message", e);
			restResponse = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
			// Audit LOG the failure
			auditFailure("handleAsync");
		}
		return restResponse;
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
	 * This method will create session context by given request.
	 * 
	 * @param request
	 *            the HttpServletRequest
	 * @return SessionContext
	 * @throws Exception 
	 * @throws JAXBException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	protected SessionContext createSessionContext(HttpServletRequest request,String mailboxPguid) throws JsonParseException, JsonMappingException, JAXBException, Exception {		
		SessionContext sessionContext = new SessionContext();
		sessionContext.copyFrom(request);
		sessionContext.setPipelineId(retrievePipelineId(mailboxPguid));
		sessionContext.setMailboxId(mailboxPguid);
		return sessionContext;
	}

	/**
	 * This method will set globalProcessId to sessionContext.
	 * 
	 * @param sessionContext
	 */
	protected void assignGlobalProcessId(SessionContext sessionContext) {
		UUIDGen uuidGen = new UUIDGen();
		String uuid = uuidGen.getUUID();
		sessionContext.setGlobalProcessId(uuid);
	}

	protected void assignTimestamp(SessionContext sessionContext) {
		sessionContext.setRequestTimestamp(new Date());
	}

	/**
	 * This method will store the payload file name by given request.
	 * 
	 * @param request
	 * @param sessionContext
	 * @throws IOException
	 */
	protected void storePayload(HttpServletRequest request,
			SessionContext sessionContext) throws IOException {
		String payloadFileName = createPayloadFileName(sessionContext);
		sessionContext.setPayloadUri(createPayloadUri(payloadFileName));
		ensurePayloadDirExists(payloadFileName);

		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			inputStream = request.getInputStream();
			outputStream = new FileOutputStream(payloadFileName);
			StreamUtil.copyStream(inputStream, outputStream);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.flush();
				} catch (IOException e) {
					logger.error(
							"Could not flush the output stream while store payload file",
							e);
				}
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.error(
							"Could not close the output stream while store payload file",
							e);
				}
			}

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error(
							"Could not close the input stream while store payload file",
							e);
				}
			}
		}
	}

	/**
	 * This method will create payload file name by given sessionContext.
	 * 
	 * @param sessionContext
	 * @return payloadFileName
	 */
	protected String createPayloadFileName(SessionContext sessionContext) {
		StringBuilder payloadFileName = new StringBuilder();
		String payloadDirectory = getHttpAsyncPayloadDirectory();

		payloadFileName.append(payloadDirectory);
		payloadFileName.append(File.separatorChar);

		if (sessionContext.getPipelineId() != null) {
			payloadFileName.append(sessionContext.getPipelineId());
			payloadFileName.append(File.separatorChar);
		}

		if (sessionContext.getDocumentProtocol() != null) {
			payloadFileName.append(sessionContext.getDocumentProtocol());
			payloadFileName.append(File.separatorChar);
		}

		if (sessionContext.getDocumentType() != null) {
			payloadFileName.append(sessionContext.getDocumentType());
			payloadFileName.append(File.separatorChar);
		}

		payloadFileName.append(sessionContext.getGlobalProcessId());
		payloadFileName.append(".http_async_payload");

		return payloadFileName.toString();
	}

	/**
	 * This method will create Payload Uri by given payloadFileName.
	 * 
	 * @param payloadFileName
	 * @return payloadUri
	 */
	protected String createPayloadUri(String payloadFileName) {
		StringBuilder payloadUri = new StringBuilder();

		payloadUri.append("file://");
		payloadUri.append(payloadFileName);

		return payloadUri.toString();
	}

	/**
	 * This method will validate Payload Directory by given payload File Name.
	 * 
	 * @param payloadFileName
	 */
	protected void ensurePayloadDirExists(String payloadFileName) {
		File payloadFile = new File(payloadFileName);
		File payloadDir = payloadFile.getParentFile();

		if (!payloadDir.exists() && !payloadDir.mkdirs()) {
			throw new RuntimeException(String.format(
					"Failed to create payload directory '%s'",
					payloadDir.getAbsolutePath()));
		}
	}

	/**
	 * This method will retrieve Payload Directory.
	 * 
	 * @return payloadDirectory
	 */
	protected String getHttpAsyncPayloadDirectory() {
		DecryptableConfiguration config = LiaisonConfigurationFactory
				.getConfiguration();
		String payloadDirectory = config
				.getString(CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR);

		if (payloadDirectory == null) {
			throw new RuntimeException(
					"HTTP Async payload directory not configured ('"
							+ CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR
							+ "'), cannot process async");
		}

		File fPayloadDirectory = new File(payloadDirectory);

		if (!(fPayloadDirectory.isDirectory() && fPayloadDirectory.canWrite())) {
			throw new RuntimeException(
					"HTTP Async payload directory configuration ('"
							+ CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR
							+ "') value ('"
							+ payloadDirectory
							+ "') is invalid (not a directory or not writeable).");
		}

		return payloadDirectory;
	}

	protected void createWorkTicket(HttpServletRequest request,
			SessionContext sessionContext) throws Exception {
		String workTicket = JAXBUtility.marshalToJSON(sessionContext);
		postToQueue(workTicket);
	}


	protected void postToQueue(String message) throws Exception {
        SweeperQueue.getInstance().pushMessages(message);

	}

	/**
	 * This method will retrieve the HttpResponse from sessionContext.
	 * 
	 * @param sessionContext
	 * @param request
	 * @return HttpResponse
	 * @throws Exception
	 * @throws JAXBException
	 */
	protected HttpResponse forwardRequest(SessionContext sessionContext,HttpServletRequest request)
			throws JAXBException, Exception {
		logger.info("Starting to forward request...");
		HttpClient httpClient = createHttpClient();
		HttpPost httpRequest = createHttpRequest(request);
		sessionContext.copyTo(httpRequest);
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
		// retrieve httplistener pipelineid from mailbox and append it to
		// serviceBroker URI
		//String pipelineId = retrievePipelineId(mailboxPguid);
		//serviceBrokerSyncUri = serviceBrokerSyncUri + "/" + pipelineId;
		HttpPost post = new HttpPost(serviceBrokerSyncUri);

		// Set the payload.
		int contentLength = request.getContentLength();
		ContentType contentType = ContentType.parse(request.getContentType());
		HttpEntity entity = new InputStreamEntity(request.getInputStream(),
				contentLength, contentType);
		post.setEntity(entity);

		return post;
	}

	/**
	 * This method will copy all Response Information.
	 * 
	 * @param httpResponse
	 * @param builder
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	protected void copyResponseInfo(HttpResponse httpResponse,
			ResponseBuilder builder, InputStream responseInputStream) throws IllegalStateException, IOException {
		Header contentLength = httpResponse
				.getFirstHeader(HTTP_HEADER_CONTENT_LENGTH);
		int iContentLength = 0;

		if (contentLength != null) {
			logger.debug(
					"Response from Service Broker contained content length header with value: {}",
					contentLength.getValue());
			iContentLength = Integer.parseInt(contentLength.getValue());
		} else {
			logger.debug("Response from Service Broker did not contain a content length header");
		}

	    if (iContentLength > 0) {
            
            responseInputStream = httpResponse.getEntity().getContent();
            Header contentType = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_TYPE);

            if (responseInputStream != null) {
                builder.entity(responseInputStream);
            }

            if (contentType != null) {
                builder.header(contentType.getName(), contentType.getValue());
            }

            if (contentLength != null) {
                builder.header(contentLength.getName(),
                        contentLength.getValue());
            }
            

        }

        copyResponseHeaders(httpResponse, builder);	    
	
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
	 * Mailbox propery "Http Listner Auth Check Required "
	 * 
	 * @param mailboxpguid
	 * @return
	 * @throws Exception
	 */
	protected boolean isAuthenticationCheckRequired(String mailboxPguid)
			throws Exception {

		boolean isAuthCheckRequired = true;
		List<PropertyDTO> properties = retrieveMailboxProperties(mailboxPguid);
		logger.info("Verifying if httplistenerauthcheckrequired is configured in the mailbox ");
		for (PropertyDTO property : properties) {
			if (property.getName().equals(HTTP_LISTENER_AUTHENTICATION_CHECK)) {
				isAuthCheckRequired = Boolean.parseBoolean(property.getValue());
				logger.info("Property httplistenerauthcheckrequired is configured in the mailbox and set to be "+property.getValue());
	
				break;
			}
		}

		return isAuthCheckRequired;
	}
	
	/**
	 * Method to retrieve the mailbox properties from the pguid and return the list of
	 * Mailbox properties"
	 * 
	 * @param mailboxpguid
	 * @return
	 * @throws Exception
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * @throws SymmetricAlgorithmException
	 * 
	 */
	private List<PropertyDTO> retrieveMailboxProperties(String mailboxPguid)
			throws JsonParseException, JsonMappingException, JAXBException,
			Exception, SymmetricAlgorithmException {
		
		MailBoxConfigurationService mbxService = new MailBoxConfigurationService();
		MailBoxDTO mailbox = mbxService.retrieveMailboxDetails(mailboxPguid, false, null);
		return mailbox.getProperties();
	}
	
	/**
	 * Method to retrieve the mailbox properties from the pguid and return the list of
	 * Mailbox properties"
	 * 
	 * @param mailboxpguid
	 * @return
	 * @throws Exception
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JAXBException
	 * 
	 */
	private String retrievePipelineId(String mailboxPguid)
			throws JsonParseException, JsonMappingException, JAXBException,
			Exception {

		String pipelineId = null;
		List<PropertyDTO> properties = retrieveMailboxProperties(mailboxPguid);
		for (PropertyDTO property : properties) {
			if (property.getName().equals(HTTP_LISTENER_PIPELINEID)) {
				pipelineId = property.getValue();
				break;
			}
		}
        logger.info("PIPELINE ID is set to be :"+pipelineId);
		return pipelineId;
	}

}
