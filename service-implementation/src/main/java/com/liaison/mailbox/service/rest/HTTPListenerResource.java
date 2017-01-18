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

import com.google.gson.Gson;
import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
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
import com.liaison.mailbox.service.util.UserAuthCacheUtil;
import com.liaison.mailbox.service.util.UserManifestCacheUtil;
import com.liaison.mailbox.service.util.WorkTicketUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.GLOBAL_PROCESS_ID_HEADER;
import static com.liaison.mailbox.MailBoxConstants.TTL_IN_SECONDS;
import static com.liaison.mailbox.MailBoxConstants.TTL_UNIT_SECONDS;
import static com.liaison.mailbox.enums.ProcessorType.HTTPASYNCPROCESSOR;
import static com.liaison.mailbox.enums.ProcessorType.HTTPSYNCPROCESSOR;

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
	private static final String NO_PRIVILEGE = "You do not have sufficient privilege to invoke the service";
    private static final String HTTP_ASYNC_REQUEST_FAILED = "HTTP Async Request Failed: ";
    private static final String HTTP_SYNC_REQUEST_FAILED = "HTTP Sync Request Failed: ";

    private Map<String, List<String>> formValues = null;

	public Map<String, List<String>> getFormValues() {
		return formValues;
	}

	public void setFormValues(Map<String, List<String>> formValues) {
		this.formValues = formValues;
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

                //GlobalProcess ID
                String globalProcessId = UUIDGen.getCustomUUID();
                String pipelineId = null;

                // Fish tag global process id
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, globalProcessId);

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
                    ThreadContext.put(MAILBOX_ID, mailboxInfo);
				} else if (!MailBoxUtil.isEmpty(mailboxName)) {
					mailboxInfo = mailboxName;
                    ThreadContext.put(MAILBOX_NAME, mailboxInfo);
				}

				logger.info("HTTP(S)-SYNC : for the mailbox {} - Start", mailboxInfo);
				try {

                    HTTPSyncProcessor syncProcessor = new HTTPSyncProcessor();
                    Map<String, String> httpListenerProperties = syncProcessor.retrieveHttpListenerProperties(
							mailboxInfo,
							HTTPSYNCPROCESSOR,
							isMailboxIdAvailable);
                    pipelineId = WorkTicketUtil.retrievePipelineId(httpListenerProperties);

                    syncProcessor.validateRequestSize(request.getContentLength());
                    // authentication should happen only if the property "Http Listener Auth Check Required" is true
					logger.info("HTTP(S)-SYNC : Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}",
							mailboxInfo);

					if (syncProcessor.isAuthenticationCheckRequired(httpListenerProperties)) {
					    authenticationAndAuthorization(request, syncProcessor, httpListenerProperties, mailboxInfo);
					}

					logger.debug("construct workticket");
					workTicket = new WorkTicketUtil().createWorkTicket(
							getRequestProperties(request, globalProcessId),
							getRequestHeaders(request),
							httpListenerProperties);

                    if (httpListenerProperties.containsKey(TTL_IN_SECONDS)) {
					    Integer ttlNumber = Integer.parseInt(httpListenerProperties.get(TTL_IN_SECONDS));
					    workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(TTL_UNIT_SECONDS, ttlNumber));
					}

                    //GLASS LOGGING - PROCESSING
                    //Fix for GMB-502
                    glassMessage = constructGlassMessage(request,
                            workTicket,
                            ExecutionState.PROCESSING,
                            globalProcessId,
                            HTTPSYNCPROCESSOR,
                            Protocol.HTTPSYNCPROCESSOR,
                            pipelineId);
                    // Log First corner
                    glassMessage.logFirstCornerTimestamp(firstCornerTimeStamp);
                    // Log status running
                    glassMessage.logProcessingStatus(StatusType.RUNNING, "HTTP Sync Request received", MailBoxConstants.HTTPSYNCPROCESSOR);
                    new TransactionVisibilityClient().logToGlass(glassMessage); // CORNER 1 LOGGING
                    //GLASS LOGGING - PROCESSING

                    // persist payload in spectrum
					if (formValues == null) {

						try (InputStream inputStream = request.getInputStream()) {
							StorageUtilities.storePayload(inputStream, workTicket, httpListenerProperties, false);
							logger.info("HTTP(S)-SYNC : GlobalPID {}", workTicket.getGlobalProcessId());
						}
					} else {

						try (InputStream inputStream = new ByteArrayInputStream(new Gson().toJson(formValues).getBytes())) {
                            workTicket.getHeaders().putAll(formValues);
                            StorageUtilities.storePayload(inputStream, workTicket, httpListenerProperties, false);
                            logger.info("HTTP(S)-SYNC : GlobalPID {}", workTicket.getGlobalProcessId());
                        }
					}

					Response syncResponse = syncProcessor.processRequest(workTicket, httpListenerProperties, request.getContentType());
					logger.info("HTTP(S)-SYNC : Status code received from service broker {} for the mailbox {}",
					        syncResponse.getStatus(),
							mailboxInfo);

                    // GLASS LOGGING //
                    if (syncResponse.getStatus() > 299) {
						glassMessage.logProcessingStatus(StatusType.ERROR,
								"HTTP Sync Request failed",
								MailBoxConstants.HTTPSYNCPROCESSOR,
								(syncResponse.getEntity() != null) ? syncResponse.getEntity().toString(): null);
					} else {

                        GlassMessage successMessage = constructGlassMessage(
                        		request,
								workTicket,
								ExecutionState.COMPLETED,
								globalProcessId,
								HTTPSYNCPROCESSOR,
                                Protocol.HTTPSYNCPROCESSOR,
                                pipelineId);
                        successMessage.logProcessingStatus(StatusType.SUCCESS, "HTTP Sync Request success", MailBoxConstants.HTTPSYNCPROCESSOR);

                        //set outbound size
						if (syncProcessor.getPayloadSize() != 0
								&& syncProcessor.getPayloadSize() != -1) {
							successMessage.setOutSize(syncProcessor.getPayloadSize());
						}

						successMessage.logFourthCornerTimestamp();
                        new TransactionVisibilityClient().logToGlass(successMessage);
                    }

                    logger.info("HTTP(S)-SYNC : for the mailbox {} - End", mailboxInfo);
					return syncResponse;
				} catch(Throwable t) {

                    logger.error(t.getMessage(), t);
                    return logLensFailure(globalProcessId,
                            ProcessorType.HTTPSYNCPROCESSOR,
                            Protocol.HTTPSYNCPROCESSOR,
                            workTicket,
                            t,
                            request,
                            pipelineId,
                            isMailboxIdAvailable,
                            mailboxInfo);

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
		return process(request, worker);
	}

	/**
	 * Handles the application/x-www-form-urlencoded requests
	 *
	 * @param mailboxId mailbox guid
	 * @param mailboxName mailbox name
	 * @param formValues form values
     * @return The Response Object
     */
	@POST
	@Path("sync")
	@AccessDescriptor(skipFilter = true)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response handleSync(@Context final HttpServletRequest request,
							   @QueryParam(value = MAILBOX_ID) final String mailboxId,
							   @QueryParam(value = MAILBOX_NAME) final String mailboxName,
							   MultivaluedMap<String,String> formValues) {
		this.setFormValues(formValues);
		return handleSync(request, mailboxId, mailboxName);
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

                //globalProcessId
                String globalProcessId = UUIDGen.getCustomUUID();
                String pipelineId = null;

                //Fix for GMB-502
                ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, globalProcessId);

				if (StringUtils.isEmpty(mailboxId) && StringUtils.isEmpty(mailboxName)) {
					logger.error("HTTP(S)-ASYNC : Mailbox Id (OR) Mailbox Name is not passed as a query param (mailboxId OR mailboxName)");
					throw new RuntimeException("Mailbox Id (OR) Mailbox Name is not passed as a query param (mailboxId OR mailboxName)");
				}

				WorkTicket workTicket = null;
				String mailboxInfo = null;
				boolean isMailboxIdAvailable = false;

				if (!MailBoxUtil.isEmpty(mailboxId)) {
					mailboxInfo = mailboxId;
					isMailboxIdAvailable = true;
                    ThreadContext.put(MAILBOX_ID, mailboxInfo);
				} else if (!MailBoxUtil.isEmpty(mailboxName)) {
					mailboxInfo = mailboxName;
                    ThreadContext.put(MAILBOX_NAME, mailboxInfo);
				}
				logger.info("HTTP(S)-ASYNC : for the mailbox {} - Start", mailboxInfo);

				try {
				    HTTPAsyncProcessor asyncProcessor = new HTTPAsyncProcessor();

                    Map<String, String> httpListenerProperties = asyncProcessor.retrieveHttpListenerProperties(
							mailboxInfo, ProcessorType.HTTPASYNCPROCESSOR, isMailboxIdAvailable);
                    pipelineId = WorkTicketUtil.retrievePipelineId(httpListenerProperties);
                    asyncProcessor.validateRequestSize(request.getContentLength());

					// authentication should happen only if the property "Http Listener Auth Check Required" is true
					logger.info("HTTP(S)-ASYNC : Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}",
							mailboxInfo);
                   if (asyncProcessor.isAuthenticationCheckRequired(httpListenerProperties)) {
                        authenticationAndAuthorization(request, asyncProcessor, httpListenerProperties, mailboxInfo);
                    }

                    workTicket = new WorkTicketUtil().createWorkTicket(
                            getRequestProperties(request, globalProcessId),
                            getRequestHeaders(request),
                            httpListenerProperties);

                    if (httpListenerProperties.containsKey(TTL_IN_SECONDS)) {
                        Integer ttlNumber = Integer.parseInt(httpListenerProperties.get(TTL_IN_SECONDS));
                        workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(TTL_UNIT_SECONDS, ttlNumber));
                    }

                    //GLASS LOGGING - PROCESSING
                    //Fix for GMB-502
                    GlassMessage glassMessage  = constructGlassMessage(
                            request,
                            workTicket,
                            ExecutionState.PROCESSING,
                            globalProcessId,
                            HTTPASYNCPROCESSOR,
                            Protocol.HTTPASYNCPROCESSOR,
                            pipelineId);
                    // Log First corner
                    glassMessage.logFirstCornerTimestamp(firstCornerTimeStamp);
                    // Log status running
                    glassMessage.logProcessingStatus(StatusType.RUNNING, "HTTP Async Request received", MailBoxConstants.HTTPASYNCPROCESSOR);
                    new TransactionVisibilityClient().logToGlass(glassMessage); // CORNER 1 LOGGING
                    //GLASS LOGGING - PROCESSING

                    // persist payload in spectrum
					if (formValues == null) {

						try (InputStream inputStream = request.getInputStream()) {
							StorageUtilities.storePayload(inputStream, workTicket, httpListenerProperties, false);
							logger.info("HTTP(S)-ASYNC : GlobalPID {}", workTicket.getGlobalProcessId());
						}
					} else {

						try (InputStream inputStream = new ByteArrayInputStream(new Gson().toJson(formValues).getBytes())) {
							workTicket.getHeaders().putAll(formValues);
							StorageUtilities.storePayload(inputStream, workTicket, httpListenerProperties, false);
							logger.info("HTTP(S)-ASYNC : GlobalPID {}", workTicket.getGlobalProcessId());
						}
					}

					workTicket.setProcessMode(ProcessMode.ASYNC);
                    //persists workticket
                    StorageUtilities.persistWorkTicket(workTicket, httpListenerProperties);
                    asyncProcessor.processWorkTicket(workTicket, glassMessage);
                    logger.info("HTTP(S)-ASYNC : GlobalPID {}, Posted workticket to Service Broker", globalProcessId);

					logger.info("HTTP(S)-ASYNC : for the mailbox {} - End", mailboxInfo);
                    return Response
                            .ok()
                            .status(Status.ACCEPTED)
                            .type(MediaType.TEXT_PLAIN)
                            .header(GLOBAL_PROCESS_ID_HEADER, globalProcessId)
                            .entity(String.format("Payload accepted as process ID '%s'", globalProcessId))
                            .build();

				} catch (Throwable e) {

                    logger.error(e.getMessage(), e);
                    return logLensFailure(globalProcessId,
                            ProcessorType.HTTPASYNCPROCESSOR,
                            Protocol.HTTPASYNCPROCESSOR,
                            workTicket,
                            e,
                            request,
                            pipelineId,
                            isMailboxIdAvailable,
                            mailboxInfo);
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
		return process(request, worker);
	}

    /**
	 * Handles the application/x-www-form-urlencoded requests
	 *
	 * @param mailboxId mailbox guid
	 * @param mailboxName mailbox name
	 * @param formValues form values
	 * @return The Response Object
	 */
	@POST
	@Path("async")
	@AccessDescriptor(skipFilter = true)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response handleAsync(@Context final HttpServletRequest request,
							   @QueryParam(value = MAILBOX_ID) final String mailboxId,
							   @QueryParam(value = MAILBOX_NAME) final String mailboxName,
							   MultivaluedMap<String,String> formValues) {
		this.setFormValues(formValues);
		return handleAsync(request, mailboxId, mailboxName);
	}

	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		return new DefaultAuditStatement(AuditStatement.Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
				PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
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
            UserAuthCacheUtil.authenticate(basicAuthenticationHeader);

            authorization(httpListenerProperties, authenticationCredentials, processorType);
        } else {
            throw new MailBoxServicesException("Authorization Header is not available in the Request", Status.UNAUTHORIZED);
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

    /**
     * Helper method to construct glass message for various cases
     *
     * @param request - http request to get content length
     * @return GlassMessage
     */
    private GlassMessage constructGlassMessage(
            final HttpServletRequest request,
            final WorkTicket workTicket,
            final ExecutionState state,
            final String globalProcessId,
            final ProcessorType type,
            final Protocol protocol,
            final String pipelineId) {

        GlassMessage glassMessage = new GlassMessage();
        glassMessage.setCategory(type);
        glassMessage.setProtocol(protocol.getCode());
        glassMessage.setProcessId(UUIDGen.getCustomUUID());

        if (null != workTicket) {

            glassMessage.setMailboxId(workTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_ID).toString());
            glassMessage.setGlobalPId(workTicket.getGlobalProcessId());
            glassMessage.setMailboxName(workTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_NAME).toString());
        } else {

            glassMessage.setGlobalPId(globalProcessId);
            glassMessage.setInAgent(GatewayType.REST);
            glassMessage.setArrivalTime(true);
        }

        if (ExecutionState.PROCESSING.equals(state)) {

            glassMessage.setInboundPipelineId(pipelineId);
            glassMessage.setStatus(ExecutionState.PROCESSING);
            glassMessage.setInAgent(GatewayType.REST);
            glassMessage.setInSize((long) request.getContentLength());

            //sets sender Ip
            glassMessage.setSenderIp(getRemoteAddress(request));
            glassMessage.setOrganizationDetails(pipelineId);
        } else if (ExecutionState.COMPLETED.equals(state)) {

            glassMessage.setStatus(ExecutionState.COMPLETED);
            glassMessage.setOutAgent(GatewayType.REST);

            //sets receiver Ip
            glassMessage.setReceiverIp(getRemoteAddress(request));
        } else if (ExecutionState.FAILED.equals(state)) {

            glassMessage.setStatus(ExecutionState.FAILED);
            glassMessage.setOutAgent(GatewayType.REST);

            //sets receiver Ip
            glassMessage.setReceiverIp(getRemoteAddress(request));
            if (!MailBoxUtil.isEmpty(pipelineId)) {
                glassMessage.setOrganizationDetails(pipelineId);
            }
        }

        return glassMessage;
    }

    /**
     * logs lens failure for http case
     *
     * @param globalProcessId global process id
     * @param processorType processor type
     * @param protocol protocol
     * @param workTicket workticket
     * @param e exception
     * @param request http request
     * @param pipelineId pipeline id
     * @param isMailboxIdAvailable boolean true if mailbox id available otherwise false
     * @param mailboxInfo mailbox details(name or pguid)
     */
    private Response logLensFailure(String globalProcessId,
                                    ProcessorType processorType,
                                    Protocol protocol,
                                    WorkTicket workTicket,
                                    Throwable e,
                                    HttpServletRequest request,
                                    String pipelineId,
                                    boolean isMailboxIdAvailable,
                                    String mailboxInfo) {

        GlassMessage glassMessage  = constructGlassMessage(
                request,
                workTicket,
                ExecutionState.FAILED,
                globalProcessId,
                processorType,
                protocol,
                pipelineId);

        //sets sender and receiver ip
        glassMessage.setSenderIp(getRemoteAddress(request));
        if (ProcessorType.HTTPSYNCPROCESSOR.equals(processorType)) {
            glassMessage.setReceiverIp(getRemoteAddress(request));
        }

        //sets mailbox details
        if (isMailboxIdAvailable) {
            glassMessage.setMailboxId(mailboxInfo);
        } else {
            glassMessage.setMailboxName(mailboxInfo);
        }

        //logs activity msg
        glassMessage.logProcessingStatus(
                StatusType.ERROR,
                (ProcessorType.HTTPSYNCPROCESSOR.equals(processorType)
                        ? HTTP_ASYNC_REQUEST_FAILED + e.getMessage()
                        : HTTP_SYNC_REQUEST_FAILED + e.getMessage()),
                processorType.name(),
                ExceptionUtils.getStackTrace(e));

        //logs tvpi status as failed
        new TransactionVisibilityClient().logToGlass(glassMessage);

        Status responseStatus = Status.INTERNAL_SERVER_ERROR;
        String responseMessage = e.getMessage();
        if (NO_PRIVILEGE.equals(responseMessage)) {
            responseStatus = Response.Status.FORBIDDEN;
            responseMessage = NO_PRIVILEGE;
        } else {
            if (e instanceof LiaisonAuditableRuntimeException) {
                responseStatus = ((LiaisonAuditableRuntimeException) e).getResponseStatus();
            }
        }

        //http response
        return Response
                .status(responseStatus)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .header(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER, globalProcessId)
                .entity(responseMessage)
                .build();

    }

}
