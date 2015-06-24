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

import java.util.Map;
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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.framework.RuntimeProcessResource;
import com.liaison.framework.util.IdentifierUtil;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.core.processor.HTTPAsyncProcessor;
import com.liaison.mailbox.service.core.processor.HTTPSyncProcessor;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.ExecutionTimestamp;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.util.WorkTicketUtil;
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

@RuntimeProcessResource
@Path("process")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class HTTPListenerResource extends AuditedResource {

	private static final Logger logger = LogManager.getLogger(HTTPListenerResource.class);

	private static final String HTTP_HEADER_BASIC_AUTH = "Authorization";

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	protected static final String HTTP_METHOD_POST = "POST";

	public HTTPListenerResource() {
		CompositeMonitor<?> monitor = Monitors.newObjectMonitor(this);
		MonitorRegistry monitorRegistry = DefaultMonitorRegistry.getInstance();
		monitorRegistry.register(monitor);
	}

	@POST
	@Path("sync/{token1}")
	public Response handleSyncOneToken(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleSync(request, mailboxPguid);
	}

	@POST
	@Path("sync/{token1}/{token2}")
	public Response handleSyncTwoTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleSync(request, mailboxPguid);
	}

	@POST
	@Path("sync/{token1}/{token2}/{token3}")
	public Response handleSyncThreeTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleSync(request, mailboxPguid);
	}

	/**
	 * This method will processing the sync message by give request.
	 *
	 * @param request The HttpServletRequest
	 * @return The Response Object
	 */
	@POST
	@Path("sync")
	@AccessDescriptor(skipFilter = true)
	public Response handleSync(@Context final HttpServletRequest request,
			@QueryParam(value = "mailboxId") final String mailboxPguid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

			    //first corner timestamp
		        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

				serviceCallCounter.incrementAndGet();
				GlassMessage glassMessage = new GlassMessage();
				// Log First corner
				glassMessage.logFirstCornerTimestamp();

				TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
				glassMessage.setCategory(ProcessorType.HTTPSYNCPROCESSOR);
				glassMessage.setProtocol(Protocol.HTTPSYNCPROCESSOR.getCode());
				glassMessage.setMailboxId(mailboxPguid);
				glassMessage.setStatus(ExecutionState.PROCESSING);
				glassMessage.setInAgent(GatewayType.REST);
				glassMessage.setInSize(request.getContentLength());

				logger.info("HTTP(S)-SYNC : for the mailbox id {} - Start", mailboxPguid);
				try {
					HTTPSyncProcessor syncProcessor = new HTTPSyncProcessor();
					syncProcessor.validateRequestSize(request.getContentLength());
					if (StringUtils.isEmpty(mailboxPguid)) {
					    logger.error("HTTP(S)-SYNC : Mailbox ID is not passed as a query param (mailboxId)");
						throw new RuntimeException("Mailbox ID is not passed as a query param (mailboxId)");
					}

					Map<String, String> httpListenerProperties = syncProcessor.retrieveHttpListenerProperties(
							mailboxPguid, ProcessorType.HTTPSYNCPROCESSOR);

					// authentication should happen only if the property "Http Listner Auth Check Required" is true
					logger.info("HTTP(S)-SYNC : Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}",
							mailboxPguid);
					if (syncProcessor.isAuthenticationCheckRequired(httpListenerProperties)) {
					    logger.info("HTTP(S)-SYNC : HTTP auth check enabled for the mailbox {}", mailboxPguid);
						syncProcessor.authenticateRequestor(request.getHeader(HTTP_HEADER_BASIC_AUTH));
					}

					logger.debug("construct workticket");
					WorkTicket workTicket = new WorkTicketUtil().createWorkTicket(getRequestProperties(request),
							getRequestHeaders(request), mailboxPguid, httpListenerProperties);
					String processId = IdentifierUtil.getUuid();
					glassMessage.setProcessId(processId);
					glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
					glassMessage.setPipelineId(workTicket.getPipelineId());

					// Log status running
					glassMessage.logProcessingStatus(StatusType.RUNNING, "HTTP Sync Request received");
					transactionVisibilityClient.logToGlass(glassMessage); // CORNER 1 LOGGING

					logger.info("HTTP(S)-SYNC : GlobalPID {}", workTicket.getGlobalProcessId());
					Response syncResponse = syncProcessor.processRequest(workTicket, request.getInputStream(),
							httpListenerProperties, request.getContentType(), mailboxPguid);
					logger.info("HTTP(S)-SYNC : Status code received from service broker {} for the mailbox {}",
					        syncResponse.getStatus(),
					        mailboxPguid);
                    // GLASS LOGGING TVAPI //
                    if (syncResponse.getStatus() > 299) {
                        glassMessage.logProcessingStatus(StatusType.ERROR, "HTTP Sync Request failed: " + syncResponse.getEntity());;
                    } else {
                        glassMessage.logProcessingStatus(StatusType.SUCCESS, "HTTP Sync Request success");
                    }
                    glassMessage.logFourthCornerTimestamp();

					return syncResponse;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					// Log error status
					glassMessage.logProcessingStatus(StatusType.ERROR, "HTTP Sync Request Failed: " + e.getMessage());
					glassMessage.setStatus(ExecutionState.FAILED);
					transactionVisibilityClient.logToGlass(glassMessage);
					glassMessage.logFourthCornerTimestamp();
					// throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
					throw new LiaisonRuntimeException(Messages.COMMON_SYNC_ERROR_MESSAGE.value());
				}
			}
		};
		worker.actionLabel = "HttpListener.handleSync()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, mailboxPguid);
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
	public Response handleAsyncOneToken(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleAsync(request, mailboxPguid);
	}

	@POST
	@Path("async/{token1}/{token2}")
	public Response handleAsyncTwoTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleAsync(request, mailboxPguid);
	}

	@POST
	@Path("async/{token1}/{token2}/{token3}")
	public Response handleAsyncThreeTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = "mailboxId") String mailboxPguid) {
		return handleAsync(request, mailboxPguid);
	}

	/**
	 * This method will processing the async message by give request.
	 *
	 * @param request The HttpServletRequest
	 * @return The Response Object
	 */
	@POST
	@Path("async")
	@AccessDescriptor(skipFilter = true)
	public Response handleAsync(@Context final HttpServletRequest request,
			@QueryParam(value = "mailboxId") final String mailboxPguid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

			    //first corner timestamp
                ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

				serviceCallCounter.incrementAndGet();

				logger.debug("Starting async processing");
				TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
				GlassMessage glassMessage = new GlassMessage();

				try {
				    HTTPAsyncProcessor asyncProcessor = new HTTPAsyncProcessor();
					asyncProcessor.validateRequestSize(request.getContentLength());
					if (StringUtils.isEmpty(mailboxPguid)) {
					    logger.info("HTTP(S)-ASYNC : Mailbox ID is not passed as a query param (mailboxId)");
						throw new RuntimeException("Mailbox ID is not passed as a query param (mailboxId)");
					}
					Map<String, String> httpListenerProperties = asyncProcessor.retrieveHttpListenerProperties(
							mailboxPguid, ProcessorType.HTTPASYNCPROCESSOR);
					// authentication should happen only if the property "Http Listner Auth Check Required" is true
					logger.info("HTTP(S)-ASYNC : Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}",
                            mailboxPguid);
					if (asyncProcessor.isAuthenticationCheckRequired(httpListenerProperties)) {
					    logger.info("HTTP(S)-ASYNC : HTTP auth check enabled for the mailbox {}", mailboxPguid);
						asyncProcessor.authenticateRequestor(request.getHeader(HTTP_HEADER_BASIC_AUTH));
					}

					WorkTicket workTicket = new WorkTicketUtil().createWorkTicket(getRequestProperties(request),
							getRequestHeaders(request), mailboxPguid, httpListenerProperties);

					String processId = IdentifierUtil.getUuid();
					glassMessage.setCategory(ProcessorType.HTTPASYNCPROCESSOR);
					glassMessage.setProtocol(Protocol.HTTPASYNCPROCESSOR.getCode());
					glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
					glassMessage.setMailboxId(mailboxPguid);
					glassMessage.setStatus(ExecutionState.PROCESSING);
					glassMessage.setPipelineId(workTicket.getPipelineId());
					glassMessage.setInAgent(GatewayType.REST);
					glassMessage.setInSize(request.getContentLength());
					glassMessage.setProcessId(processId);

					// Log FIRST corner
					glassMessage.logFirstCornerTimestamp(firstCornerTimeStamp);

					// Log running status
					glassMessage.logProcessingStatus(StatusType.RUNNING, "HTTP ASync Request success");

					// Log TVA status
					transactionVisibilityClient.logToGlass(glassMessage);

					StorageUtilities.storePayload(request.getInputStream(), workTicket, httpListenerProperties, false);
					workTicket.setProcessMode(ProcessMode.ASYNC);
					logger.info("HTTP(S)-ASYNC : GlobalPID {}", workTicket.getGlobalProcessId());
					asyncProcessor.processWorkTicket(workTicket, mailboxPguid, glassMessage);
					logger.info("HTTP(S)-ASYNC : GlobalPID {}, Posted workticket to Service Broker", workTicket.getGlobalProcessId());

					logger.info("HTTP(S)-ASYNC : for the mailbox id {} - End", mailboxPguid);
					return Response.ok().status(Status.ACCEPTED).type(MediaType.TEXT_PLAIN).entity(
							String.format("Payload accepted as process ID '%s'", workTicket.getGlobalProcessId())).build();

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					// Log error status
					glassMessage.logProcessingStatus(StatusType.ERROR, "HTTP ASync Request Failed: " + e.getMessage());
					glassMessage.setStatus(ExecutionState.FAILED);
					transactionVisibilityClient.logToGlass(glassMessage);
					glassMessage.logFourthCornerTimestamp();
					throw new LiaisonRuntimeException(Messages.COMMON_SYNC_ERROR_MESSAGE.value());
				}
			}
		};
		worker.actionLabel = "HttpListener.handleAsync()";
		// Added the guid
		worker.queryParams.put(AuditedResource.HEADER_GUID, mailboxPguid);
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
