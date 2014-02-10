package com.liaison.mailbox.service.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import oracle.jdbc.proxy.annotation.Post;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.StreamUtil;
import com.liaison.commons.util.UUIDGen;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;
import com.liaison.mailbox.service.util.HornetQJMSUtil;
import com.liaison.mailbox.service.util.SessionContext;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.CompositeMonitor;
import com.netflix.servo.monitor.Monitors;


/**
 * G2 HTTP Gateway.
 */
@Path("process")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class HttpListener
{
	private static final Logger logger = LoggerFactory.getLogger(HttpListener.class);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);


	protected static final String HTTP_METHOD_POST = "POST";

	protected static final String CONFIGURATION_SERVICE_BROKER_URI = "com.liaison.servicebroker.sync.uri";
	protected static final String CONFIGURATION_MAX_REQUEST_SIZE = "com.liaison.servicebroker.sync.max.request.size";
	protected static final String CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR = "com.liaison.mailbox.http.async.payload.dir";
	protected static final String CONFIGURATION_QUEUE_PROVIDER_URL = "providerurl";
	protected static final String CONFIGURATION_QUEUE_NAME = "queuename";
	protected static final String HTTP_HEADER_BASIC_AUTH = "Authorization";
	protected static final String GATEWAY_HEADER_PREFIX = "x-gate-";
	protected static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	protected static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";


	public HttpListener ()
	{
		CompositeMonitor<?> monitor = Monitors.newObjectMonitor(this);
		MonitorRegistry monitorRegistry = DefaultMonitorRegistry.getInstance();
		monitorRegistry.register(monitor);
	}

	@POST
	@Path("sync/{token1}")
	public Response handleSyncOneToken (@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		return handleSync(request);
	}

	@POST
	@Path("sync/{token1}/{token2}")
	public Response handleSyncTwoTokens (@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		return handleSync(request);
	}

	@POST
	@Path("sync/{token1}/{token2}/{token3}")
	public Response handleSyncThreeTokens (@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		return handleSync(request);
	}

	@POST
	@Path("sync")
	public Response handleSync (@Context HttpServletRequest request)
	{
		Response restResponse = null;
		serviceCallCounter.incrementAndGet();

		logger.info("Starting sync processing");
		try
		{
			validateRequestSize(request);
			authenticateRequestor(request);
			SessionContext sessionContext = createSessionContext(request);
			assignGlobalProcessId(sessionContext);
			assignTimestamp(sessionContext);

			HttpResponse httpResponse = forwardRequest(sessionContext, request);
			ResponseBuilder builder = Response.ok();
			copyResponseInfo(httpResponse, builder);
			restResponse = builder.build();
		}
		catch (Exception e)
		{
			logger.error("Error processing sync message", e);
			e.printStackTrace();
			restResponse = Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return restResponse;
	}

	@POST
	@Path("async/{token1}")
	public Response handleAsyncOneToken (@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		return handleAsync(request);
	}

	@POST
	@Path("async/{token1}/{token2}")
	public Response handleAsyncTwoTokens (@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		return handleAsync(request);
	}

	@POST
	@Path("async/{token1}/{token2}/{token3}")
	public Response handleAsyncThreeTokens (@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		return handleAsync(request);
	}

	@Post
	@Path("async")
	public Response handleAsync (@Context HttpServletRequest request)
	{
		Response restResponse = null;
		serviceCallCounter.incrementAndGet();

		logger.info("Starting async processing");
		try
		{
			validateRequestSize(request);
			authenticateRequestor(request);
			SessionContext sessionContext = createSessionContext(request);
			assignGlobalProcessId(sessionContext);
			assignTimestamp(sessionContext);

			storePayload(request, sessionContext);
			createWorkTicket(request, sessionContext);

			restResponse = Response.ok()
				                   .status(Status.ACCEPTED)
					               .type(MediaType.TEXT_PLAIN)
					               .entity(String.format("Payload accepted as process ID '%s'", sessionContext.getGlobalProcessId()))
					               .build();
		}
		catch (Exception e)
		{
			logger.error("Error processing async message", e);
			e.printStackTrace();
			restResponse = Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return restResponse;
	}

	protected void validateRequestSize (HttpServletRequest request)
	{
		long contentLength = request.getContentLength();
		DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();
		int maxRequestSize = config.getInt(CONFIGURATION_MAX_REQUEST_SIZE);

		if (contentLength > maxRequestSize)
		{
			throw new RuntimeException("Request has content length of " + contentLength + " which exceeds the configured maximum size of " + maxRequestSize);
		}
	}

	protected void authenticateRequestor (HttpServletRequest request)
	{
		// TODO - when User Management service is available.
	}

	protected SessionContext createSessionContext (HttpServletRequest request)
	{
		SessionContext sessionContext = new SessionContext();
		sessionContext.copyFrom(request);
		return sessionContext;
	}

	protected void assignGlobalProcessId (SessionContext sessionContext)
	{
		UUIDGen uuidGen = new UUIDGen();
		String uuid = uuidGen.getUUID();
		sessionContext.setGlobalProcessId(uuid);
	}

	protected void assignTimestamp (SessionContext sessionContext)
	{
		sessionContext.setRequestTimestamp(new Date());
	}

	protected void storePayload (HttpServletRequest request, SessionContext sessionContext)
		throws IOException
	{
		String payloadFileName = createPayloadFileName(sessionContext);
		sessionContext.setPayloadUri(createPayloadUri(payloadFileName));
		ensurePayloadDirExists(payloadFileName);

		InputStream inputStream = null;
		OutputStream outputStream = null;

		try
		{
			inputStream = request.getInputStream();
			outputStream = new FileOutputStream(payloadFileName);
			StreamUtil.copyStream(inputStream, outputStream);
		}
		finally
		{
			if (outputStream != null)
			{
				try { outputStream.flush(); } catch (IOException e) { /* ignore */ }
				try { outputStream.close(); } catch (IOException e) { /* ignore */ }
			}

			if (inputStream != null)
			{
				try { inputStream.close(); } catch (IOException e) { /* ignore */ }
			}
		}
	}

	protected String createPayloadFileName (SessionContext sessionContext)
	{
		StringBuilder payloadFileName = new StringBuilder();
		String payloadDirectory = getHttpAsyncPayloadDirectory();

		payloadFileName.append(payloadDirectory);
		payloadFileName.append(File.separatorChar);

		if (sessionContext.getPipelineId() != null)
		{
			payloadFileName.append(sessionContext.getPipelineId());
			payloadFileName.append(File.separatorChar);
		}

		if (sessionContext.getDocumentProtocol() != null)
		{
			payloadFileName.append(sessionContext.getDocumentProtocol());
			payloadFileName.append(File.separatorChar);
		}

		if (sessionContext.getDocumentType() != null)
		{
			payloadFileName.append(sessionContext.getDocumentType());
			payloadFileName.append(File.separatorChar);
		}

		payloadFileName.append(sessionContext.getGlobalProcessId());
		payloadFileName.append(".http_async_payload");

		return payloadFileName.toString();
	}

	protected String createPayloadUri (String payloadFileName)
	{
		StringBuilder payloadUri = new StringBuilder();

		payloadUri.append("file://");
		payloadUri.append(payloadFileName);

		return payloadUri.toString();
	}

	protected void ensurePayloadDirExists (String payloadFileName)
	{
		File payloadFile = new File(payloadFileName);
		File payloadDir = payloadFile.getParentFile();

		if (!payloadDir.exists())
		{
			if (!payloadDir.mkdirs())
			{
				throw new RuntimeException(String.format("Failed to create payload directory '%s'", payloadDir.getAbsolutePath()));
			}
		}
	}

	protected String getHttpAsyncPayloadDirectory ()
	{
		DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();
		String payloadDirectory = config.getString(CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR);

		if (payloadDirectory == null)
		{
			throw new RuntimeException("HTTP Async payload directory not configured ('" + CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR + "'), cannot process async");
		}

		File fPayloadDirectory = new File(payloadDirectory);

		if (!(fPayloadDirectory.isDirectory() && fPayloadDirectory.canWrite()))
		{
			throw new RuntimeException("HTTP Async payload directory configuration ('" +
		                               CONFIGURATION_HTTP_ASYNC_PAYLOAD_DIR +
		                               "') value ('" +
		                               payloadDirectory +
		                               "') is invalid (not a directory or not writeable).");
		}

		return payloadDirectory;
	}

	protected void createWorkTicket (HttpServletRequest request, SessionContext sessionContext)
		throws Exception
	{
		String workTicket = JAXBUtility.marshalToJSON(sessionContext);
		postToQueue(workTicket);
	}

	protected void postToQueue (String message)
		throws Exception
	{
		DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();
		String providerURL = config.getString(CONFIGURATION_QUEUE_PROVIDER_URL);
		String queueName = config.getString(CONFIGURATION_QUEUE_NAME);;

		ConfigureJNDIDTO jndidto = new ConfigureJNDIDTO();
		jndidto.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
		jndidto.setProviderURL(providerURL);
		jndidto.setQueueName(queueName);
		jndidto.setUrlPackagePrefixes("org.jboss.naming");
		jndidto.setMessage(message);

		HornetQJMSUtil.postMessage(jndidto);
	}

	protected HttpResponse forwardRequest (SessionContext sessionContext, HttpServletRequest request)
		throws ClientProtocolException, IOException
	{
		HttpClient httpClient = createHttpClient();
		HttpPost httpRequest = createHttpRequest(request);
		sessionContext.copyTo(httpRequest);
		HttpResponse httpResponse = httpClient.execute(httpRequest);
		return httpResponse;
	}

	protected HttpClient createHttpClient ()
	{
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		HttpClient httpClient = httpClientBuilder.build();
		return httpClient;
	}

	protected HttpPost createHttpRequest (HttpServletRequest request)
		throws IOException
	{
		String serviceBrokerSyncUri = getServiceBrokerUriFromConfig();
		HttpPost post = new HttpPost(serviceBrokerSyncUri);

		// Set the payload.
		int contentLength = request.getContentLength();
		ContentType contentType = ContentType.parse(request.getContentType());
		HttpEntity entity = new InputStreamEntity(request.getInputStream(), contentLength, contentType);
		post.setEntity(entity);

		return post;
	}

	protected void copyResponseInfo (HttpResponse httpResponse, ResponseBuilder builder)
		throws IllegalStateException, IOException
	{
		Header contentLength = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_LENGTH);
		int iContentLength = Integer.parseInt(contentLength.getValue());

		if (iContentLength > 0)
		{
			InputStream responseInputStream = httpResponse.getEntity().getContent();
			Header contentType = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_TYPE);
//			ContentType contentType = getResponseContentType(httpResponse);
//			InputStreamEntity entity = new InputStreamEntity(responseInputStream, contentLength, contentType);
//			builder.entity(entity);
			builder.entity(responseInputStream);
			builder.header(contentType.getName(), contentType.getValue());
			builder.header(contentLength.getName(), contentLength.getValue());
		}

		copyResponseHeaders(httpResponse, builder);
	}

	protected int getResponseContentLength (HttpResponse httpResponse)
	{
		Header header = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_LENGTH);

		if (header == null)
		{
			// TODO - this should be an error.
			return 0;
		}

		String strLength = header.getValue();
		int length = Integer.parseInt(strLength);

		return length;
	}

	protected ContentType getResponseContentType (HttpResponse httpResponse)
	{
		Header header = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_TYPE);

		if (header == null)
		{
			// TODO - this should be an error.
			return null;
		}

		String strContentType = header.getValue();
		ContentType contentType = ContentType.parse(strContentType);

		return contentType;
	}

	protected void copyResponseHeaders (HttpResponse httpResponse, ResponseBuilder builder)
	{
		// Copy headers that start with the Gateway prefix.
		Header[] headers = httpResponse.getAllHeaders();

		for (Header header : headers)
		{
			if (header.getName().regionMatches(true, 0, GATEWAY_HEADER_PREFIX, 0, GATEWAY_HEADER_PREFIX.length()))
			{
				String name = header.getName();
				name = name.substring(GATEWAY_HEADER_PREFIX.length());
				builder.header(name, header.getValue());
			}
		}
	}

	protected String getServiceBrokerUriFromConfig ()
	{
		DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();
		String serviceBrokerUri = config.getString(CONFIGURATION_SERVICE_BROKER_URI);

		if ((serviceBrokerUri == null) || (serviceBrokerUri.trim().length() == 0))
		{
			throw new RuntimeException("Service Broker URI not configured ('" + CONFIGURATION_SERVICE_BROKER_URI + "'), cannot process sync");
		}

		return serviceBrokerUri;
	}

}
