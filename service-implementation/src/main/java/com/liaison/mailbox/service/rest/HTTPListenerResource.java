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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
import org.apache.logging.log4j.ThreadContext;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.util.UUIDGen;
import com.liaison.dto.enums.ProcessMode;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.framework.RuntimeProcessResource;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.core.processor.HTTPAbstractProcessor;
import com.liaison.mailbox.service.core.processor.HTTPAsyncProcessor;
import com.liaison.mailbox.service.core.processor.HTTPSyncProcessor;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;
import com.liaison.mailbox.service.glass.util.GlassMessage;
import com.liaison.mailbox.service.glass.util.TransactionVisibilityClient;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.UserManifestCacheUtil;
import com.liaison.mailbox.service.util.WorkTicketUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.CompositeMonitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.stats.StatsConfig;

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
	private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	private static final String NO_PRIVILEGE = "You do not have sufficient privilege to invoke the service";

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

    private Stopwatch stopwatch;
    private static final StatsTimer statsTimer = new StatsTimer(
            MonitorConfig.builder("HTTPListenerResource_statsTimer").build(),
            new StatsConfig.Builder().build());

    static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }

	public HTTPListenerResource() {
		CompositeMonitor<?> monitor = Monitors.newObjectMonitor(this);
		MonitorRegistry monitorRegistry = DefaultMonitorRegistry.getInstance();
		monitorRegistry.register(monitor);
	}

	@POST
	@Path("sync/{token1}")
	public Response handleSyncOneToken(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_NAME) final String mailboxName) {
		return handleSync(request, mailboxId, mailboxName);
	}

	@POST
	@Path("sync/{token1}/{token2}")
	public Response handleSyncTwoTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_NAME) final String mailboxName) {
		return handleSync(request, mailboxId, mailboxName);
	}

	@POST
	@Path("sync/{token1}/{token2}/{token3}")
	public Response handleSyncThreeTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_ID) final String mailboxName) {
		return handleSync(request, mailboxId, mailboxName);
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
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_NAME) final String mailboxName) {
		
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

			    //first corner timestamp
		        ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

				serviceCallCounter.incrementAndGet();
				GlassMessage glassMessage = null;
				WorkTicket workTicket = null;
				
				if (StringUtils.isEmpty(mailboxId) && StringUtils.isEmpty(mailboxName)) {
				    logger.error("HTTP(S)-SYNC : Mailbox Id (OR) Mailbox Name is not passed as a query param (mailboxId OR mailboxName)");
					throw new RuntimeException("Mailbox Id (OR) Mailbox Name is not passed as a query param (mailboxId OR mailboxName)");
				}
				
				String mailboxInfo = null;
				boolean isMailboxIdAvailable = false;
				
				if (!MailBoxUtil.isEmpty(mailboxId)) {
					mailboxInfo = mailboxId;
					isMailboxIdAvailable = true;
				} else if (!MailBoxUtil.isEmpty(mailboxName)) {
					mailboxInfo = mailboxName;
				}

				logger.info("HTTP(S)-SYNC : for the mailbox {} - Start", mailboxInfo);
				try {

					HTTPSyncProcessor syncProcessor = new HTTPSyncProcessor();
					syncProcessor.validateRequestSize(request.getContentLength());

					Map<String, String> httpListenerProperties = syncProcessor.retrieveHttpListenerProperties(
							mailboxInfo, ProcessorType.HTTPSYNCPROCESSOR, isMailboxIdAvailable);

					// authentication should happen only if the property "Http Listner Auth Check Required" is true
					logger.info("HTTP(S)-SYNC : Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}",
							mailboxInfo);

					if (syncProcessor.isAuthenticationCheckRequired(httpListenerProperties)) {
					    authenticationAndAuthorization(request, syncProcessor, httpListenerProperties, mailboxInfo);
					}

					logger.debug("construct workticket");
					workTicket = new WorkTicketUtil().createWorkTicket(getRequestProperties(request),
							getRequestHeaders(request), httpListenerProperties);

                    if (httpListenerProperties.containsKey(MailBoxConstants.TTL_IN_SECONDS)) {
					    Integer ttlNumber = Integer.parseInt(httpListenerProperties.get(MailBoxConstants.TTL_IN_SECONDS));
					    workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(MailBoxConstants.TTL_UNIT_SECONDS, ttlNumber));
					}

                    // persist payload in spectrum
                    try (InputStream inputStream = request.getInputStream()) {
                        StorageUtilities.storePayload(inputStream, workTicket, httpListenerProperties, false);
                        logger.info("HTTP(S)-SYNC : GlobalPID {}", workTicket.getGlobalProcessId());
                    }

                    //Fix for GMB-502
                    glassMessage = constructGlassMessage(request, workTicket, ExecutionState.PROCESSING);

                    // Fish tag global process id
                    ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());

                    //MailBox TVAPI updates should be sent only after the payload has been persisted to Spectrum
                    // Log First corner
                    glassMessage.logFirstCornerTimestamp(firstCornerTimeStamp);
                    // Log status running
                    glassMessage.logProcessingStatus(StatusType.RUNNING, "HTTP Sync Request received", MailBoxConstants.HTTPSYNCPROCESSOR);
                    new TransactionVisibilityClient().logToGlass(glassMessage); // CORNER 1 LOGGING

					Response syncResponse = syncProcessor.processRequest(workTicket, request.getInputStream(),
							httpListenerProperties, request.getContentType());
					logger.info("HTTP(S)-SYNC : Status code received from service broker {} for the mailbox {}",
					        syncResponse.getStatus(),
							mailboxInfo);

                    // GLASS LOGGING //
                    if (syncResponse.getStatus() > 299) {
                        glassMessage.logProcessingStatus(StatusType.ERROR, "HTTP Sync Request failed: " + syncResponse.getEntity(), MailBoxConstants.HTTPSYNCPROCESSOR);
                    } else {
                        GlassMessage successMessage = constructGlassMessage(request, workTicket, ExecutionState.COMPLETED);
                        successMessage.logProcessingStatus(StatusType.SUCCESS, "HTTP Sync Request success", MailBoxConstants.HTTPSYNCPROCESSOR);

                        //Hack to set outbound size
                        List<Object> contenLength = syncResponse.getMetadata().get(HTTP_HEADER_CONTENT_LENGTH);
                        if (null != contenLength && !contenLength.isEmpty()) {
                            String outSize = String.valueOf(syncResponse.getMetadata().get(HTTP_HEADER_CONTENT_LENGTH).get(0));
                            if (!MailBoxUtil.isEmpty(outSize) && !("null".equals(outSize))) {
                                successMessage.setOutSize(Long.valueOf(outSize));
                            }
                        }
                        new TransactionVisibilityClient().logToGlass(successMessage);
                    }

                    glassMessage.logFourthCornerTimestamp();
                    logger.info("HTTP(S)-SYNC : for the mailbox {} - End", mailboxInfo);
					return syncResponse;
				} catch (Exception e) {

				    String errorMessage = e.getMessage();
					logger.error(errorMessage, e);

					//MailBox TVAPI updates should be sent only after the payload has been persisted to Spectrum
					if (!Messages.PAYLOAD_ALREADY_EXISTS.value().equals(errorMessage)
                            && !Messages.PAYLOAD_PERSIST_ERROR.value().equals(errorMessage)
                            && null != glassMessage) {

				        GlassMessage failedMsg = constructGlassMessage(request, workTicket, ExecutionState.FAILED);
	                    // Log error status
				        failedMsg.logProcessingStatus(StatusType.ERROR, "HTTP Sync Request Failed: " + e.getMessage(), MailBoxConstants.HTTPSYNCPROCESSOR);
				        new TransactionVisibilityClient().logToGlass(failedMsg);
	                    glassMessage.logFourthCornerTimestamp();

                    }
					
					if (NO_PRIVILEGE.equals(e.getMessage())) {
					    throw new MailBoxServicesException(NO_PRIVILEGE, Response.Status.FORBIDDEN);
					} else {
					    throw new LiaisonRuntimeException(e.getMessage());
					}
                } finally {
                    ThreadContext.clearMap();
				}
			}

		};
		worker.actionLabel = "HttpListener.handleSync()";
		// Added the guid
		worker.queryParams.put(HEADER_GUID, mailboxId);
		worker.queryParams.put(MailBoxConstants.KEY_MAILBOX_NAME, mailboxName);
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
	 * Helper method to construct glass message for various cases
	 *
     * @param request - http request to get content length
     * @return GlassMessage
     */
    private GlassMessage constructGlassMessage(final HttpServletRequest request,
            final WorkTicket workTicket,
            final ExecutionState state) {

        GlassMessage glassMessage = new GlassMessage();
        glassMessage.setCategory(ProcessorType.HTTPSYNCPROCESSOR);
        glassMessage.setProtocol(Protocol.HTTPSYNCPROCESSOR.getCode());
        glassMessage.setMailboxId(workTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_ID).toString());
        glassMessage.setProcessId(UUIDGen.getCustomUUID());
        glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
        glassMessage.setMailboxName(workTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_NAME).toString());

		if (ExecutionState.PROCESSING.equals(state)) {
			glassMessage.setInboundPipelineId(workTicket.getPipelineId());
			glassMessage.setStatus(ExecutionState.PROCESSING);
            glassMessage.setInAgent(GatewayType.REST);
            glassMessage.setInSize((long) request.getContentLength());
        } else if (ExecutionState.COMPLETED.equals(state)) {
            glassMessage.setStatus(ExecutionState.COMPLETED);
            glassMessage.setOutAgent(GatewayType.REST);
        } else if (ExecutionState.FAILED.equals(state)) {
            glassMessage.setStatus(ExecutionState.FAILED);
        }

        return glassMessage;
    }

	@POST
	@Path("async/{token1}")
	public Response handleAsyncOneToken(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_NAME) final String mailboxName) {
		return handleAsync(request, mailboxId, mailboxName);
	}

	@POST
	@Path("async/{token1}/{token2}")
	public Response handleAsyncTwoTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_NAME) final String mailboxName) {
		return handleAsync(request, mailboxId, mailboxName);
	}

	@POST
	@Path("async/{token1}/{token2}/{token3}")
	public Response handleAsyncThreeTokens(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_NAME) final String mailboxName) {
		return handleAsync(request, mailboxId, mailboxName);
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
			@QueryParam(value = MAILBOX_ID) final String mailboxId,
			@QueryParam(value = MAILBOX_NAME) final String mailboxName) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

			    //first corner timestamp
                ExecutionTimestamp firstCornerTimeStamp = ExecutionTimestamp.beginTimestamp(GlassMessage.DEFAULT_FIRST_CORNER_NAME);

				serviceCallCounter.incrementAndGet();

				TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
				GlassMessage glassMessage = null;

				if (StringUtils.isEmpty(mailboxId) && StringUtils.isEmpty(mailboxName)) {
					logger.error("HTTP(S)-ASYNC : Mailbox Id (OR) Mailbox Name is not passed as a query param (mailboxId OR mailboxName)");
					throw new RuntimeException("Mailbox Id (OR) Mailbox Name is not passed as a query param (mailboxId OR mailboxName)");
				}

				String mailboxInfo = null;
				boolean isMailboxIdAvailable = false;

				if (!MailBoxUtil.isEmpty(mailboxId)) {
					mailboxInfo = mailboxId;
					isMailboxIdAvailable = true;
				} else if (!MailBoxUtil.isEmpty(mailboxName)) {
					mailboxInfo = mailboxName;
				}
				logger.info("HTTP(S)-ASYNC : for the mailbox {} - Start", mailboxInfo);

				try {
				    HTTPAsyncProcessor asyncProcessor = new HTTPAsyncProcessor();
					asyncProcessor.validateRequestSize(request.getContentLength());

					Map<String, String> httpListenerProperties = asyncProcessor.retrieveHttpListenerProperties(
							mailboxInfo, ProcessorType.HTTPASYNCPROCESSOR, isMailboxIdAvailable);
					
					// authentication should happen only if the property "Http Listener Auth Check Required" is true
					logger.info("HTTP(S)-ASYNC : Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}",
							mailboxInfo);
                   if (asyncProcessor.isAuthenticationCheckRequired(httpListenerProperties)) {
                        authenticationAndAuthorization(request, asyncProcessor, httpListenerProperties, mailboxInfo);
                    }

					WorkTicket workTicket = new WorkTicketUtil().createWorkTicket(getRequestProperties(request),
							getRequestHeaders(request), httpListenerProperties);

                    if (httpListenerProperties.containsKey(MailBoxConstants.TTL_IN_SECONDS)) {
                        Integer ttlNumber = Integer.parseInt(httpListenerProperties.get(MailBoxConstants.TTL_IN_SECONDS));
                        workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(MailBoxConstants.TTL_UNIT_SECONDS, ttlNumber));
                    }

                    // persist payload in spectrum
                    try (InputStream inputStream = request.getInputStream()) {
                        StorageUtilities.storePayload(inputStream, workTicket, httpListenerProperties, false);
                        logger.info("HTTP(S)-ASYNC : GlobalPID {}", workTicket.getGlobalProcessId());
                    }

                    glassMessage = new GlassMessage();
                    glassMessage.setCategory(ProcessorType.HTTPASYNCPROCESSOR);
					glassMessage.setProtocol(Protocol.HTTPASYNCPROCESSOR.getCode());
					glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
					glassMessage.setMailboxId(httpListenerProperties.get(MailBoxConstants.KEY_MAILBOX_ID));
					glassMessage.setMailboxName(httpListenerProperties.get(MailBoxConstants.KEY_MAILBOX_NAME));
					glassMessage.setStatus(ExecutionState.PROCESSING);
					glassMessage.setInboundPipelineId(workTicket.getPipelineId());
					glassMessage.setInAgent(GatewayType.REST);
					glassMessage.setInSize((long) request.getContentLength());
					glassMessage.setProcessId(UUIDGen.getCustomUUID());

                    //Fix for GMB-502
                    ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, workTicket.getGlobalProcessId());

                    //MailBox TVAPI updates should be sent only after the payload has been persisted to Spectrum
                    // Log FIRST corner
                    glassMessage.logFirstCornerTimestamp(firstCornerTimeStamp);
                    // Log running status
                    glassMessage.logProcessingStatus(StatusType.RUNNING, "HTTP Async request success", MailBoxConstants.HTTPASYNCPROCESSOR);
                    // Log TVA status
                    transactionVisibilityClient.logToGlass(glassMessage);

					workTicket.setProcessMode(ProcessMode.ASYNC);
					asyncProcessor.processWorkTicket(workTicket, glassMessage);
					logger.info("HTTP(S)-ASYNC : GlobalPID {}, Posted workticket to Service Broker", workTicket.getGlobalProcessId());

					logger.info("HTTP(S)-ASYNC : for the mailbox {} - End", mailboxInfo);
					return Response.ok().status(Status.ACCEPTED).type(MediaType.TEXT_PLAIN).entity(
							String.format("Payload accepted as process ID '%s'", workTicket.getGlobalProcessId())).build();

				} catch (Exception e) {
				    String errorMessage = e.getMessage();
                    logger.error(errorMessage, e);

                    //MailBox TVAPI updates should be sent only after the payload has been persisted to Spectrum
                    if (!Messages.PAYLOAD_ALREADY_EXISTS.value().equals(errorMessage)
                            && !Messages.PAYLOAD_PERSIST_ERROR.value().equals(errorMessage)
                            && null != glassMessage) {
                    	
                        // Log error status
                        glassMessage.logProcessingStatus(StatusType.ERROR, "HTTP Async Request Failed: " + e.getMessage(), MailBoxConstants.HTTPASYNCPROCESSOR);
                        glassMessage.setStatus(ExecutionState.FAILED);
                        transactionVisibilityClient.logToGlass(glassMessage);
                        glassMessage.logFourthCornerTimestamp();
                    }
                    
                    if (NO_PRIVILEGE.equals(e.getMessage())) {
                        throw new MailBoxServicesException(NO_PRIVILEGE, Response.Status.FORBIDDEN);
                    } else {
                        throw new LiaisonRuntimeException(e.getMessage());
                    }
                } finally {
                    ThreadContext.clearMap();
				}
			}
		};
		worker.actionLabel = "HttpListener.handleAsync()";
		// Added the guid
		worker.queryParams.put(HEADER_GUID, mailboxId);
		worker.queryParams.put(MailBoxConstants.KEY_MAILBOX_NAME, mailboxName);
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

	    stopwatch = statsTimer.start();
        int globalCount = globalServiceCallCounter.addAndGet(1);
        logKPIMetric(globalCount, "Global_serviceCallCounter");
        int serviceCount = serviceCallCounter.addAndGet(1);
        logKPIMetric(serviceCount, "HTTPListenerResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {

	    stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "HTTPListenerResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "HTTPListenerResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "HTTPListenerResource_failureCounter");
        }
	}

	/**
     * This method will authenticate and authorize the SYNC and ASYNC processor.
     *
     * @param request The HttpServletRequest
     * @param processor - Either sync or Async Processor
     * @param httpListenerProperties
     * @param mailboxInfo
	 * @throws IOException 
     */
    private void authenticationAndAuthorization(final HttpServletRequest request,
            HTTPAbstractProcessor processor, Map<String, String> httpListenerProperties, String mailboxInfo)
            throws IOException {

        String basicAuthenticationHeader = request.getHeader(HTTP_HEADER_BASIC_AUTH);
        if (!MailBoxUtil.isEmpty(basicAuthenticationHeader)) {

            String[] authenticationCredentials = HTTPAbstractProcessor.getAuthenticationCredentials(basicAuthenticationHeader);

            String processorType = processor instanceof HTTPSyncProcessor ? "SYNC" : "ASYNC";
            logger.info("HTTP(S)-"+ processorType +" : HTTP auth check enabled for the mailbox {}", mailboxInfo);
            processor.authenticateRequestor(authenticationCredentials);

            authorization(httpListenerProperties, authenticationCredentials, processorType);
        } else {
            throw new RuntimeException("Authorization Header not available in the Request");
        }
    }

    /**
     * This is the helper method for authorization of SYNC and ASYNC processor.
     *
     * @param httpListenerProperties
     * @param authenticationCredentials The HttpServletRequest
     * @param processorType - Type of the Processor
     * @throws IOException 
     */
    private void authorization(Map<String, String> httpListenerProperties, String[] authenticationCredentials, String processorType)
             throws IOException {

        boolean tenancyKeyExist = false;
        if (authenticationCredentials.length == 2) {

            String loginId = authenticationCredentials[0];

            GEMManifestResponse gemManifestResponse = UserManifestCacheUtil.getACLManifestByloginId(loginId);

            String manifest = (gemManifestResponse != null) ? gemManifestResponse.getManifest() : null;

            if (!MailBoxUtil.isEmpty(manifest)) {
                List<RoleBasedAccessControl> roleBasedAccessControl = UserManifestCacheUtil.getDomainsFromACLManifest(manifest);
                
                String tenancyKey = httpListenerProperties.get(MailBoxConstants.PROPERTY_TENANCY_KEY);

                for (RoleBasedAccessControl roleBasedAccessCtrl : roleBasedAccessControl) {

                    if (roleBasedAccessCtrl.getDomainInternalName().equals(tenancyKey)) {
                        tenancyKeyExist = true;
                        break;
                    }
                }
            } else {
                logger.info("HTTP(S)-" + processorType + " : Manifest should not be empty.");
            }
        }

        if (!tenancyKeyExist) {
            throw new RuntimeException(NO_PRIVILEGE);
        }
    }

}
