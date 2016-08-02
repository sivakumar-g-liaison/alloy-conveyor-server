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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
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

import static com.liaison.mailbox.MailBoxConstants.TTL_IN_SECONDS;
import static com.liaison.mailbox.MailBoxConstants.TTL_UNIT_SECONDS;
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
							mailboxInfo,
							HTTPSYNCPROCESSOR,
							isMailboxIdAvailable);

					// authentication should happen only if the property "Http Listner Auth Check Required" is true
					logger.info("HTTP(S)-SYNC : Verifying if httplistenerauthcheckrequired is configured in httplistener of mailbox {}",
							mailboxInfo);

					if (syncProcessor.isAuthenticationCheckRequired(httpListenerProperties)) {
					    authenticationAndAuthorization(request, syncProcessor, httpListenerProperties, mailboxInfo);
					}

					logger.debug("construct workticket");
					workTicket = new WorkTicketUtil().createWorkTicket(
							getRequestProperties(request),
							getRequestHeaders(request),
							httpListenerProperties);

                    if (httpListenerProperties.containsKey(TTL_IN_SECONDS)) {
					    Integer ttlNumber = Integer.parseInt(httpListenerProperties.get(TTL_IN_SECONDS));
					    workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(TTL_UNIT_SECONDS, ttlNumber));
					}

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

                        GlassMessage successMessage = constructGlassMessage(request, workTicket, ExecutionState.COMPLETED);
                        successMessage.logProcessingStatus(StatusType.SUCCESS, "HTTP Sync Request success", MailBoxConstants.HTTPSYNCPROCESSOR);

                        //set outbound size
						if (syncProcessor.getPayloadSize() != 0
								&& syncProcessor.getPayloadSize() != -1) {
							successMessage.setOutSize(syncProcessor.getPayloadSize());
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
						failedMsg.logProcessingStatus(StatusType.ERROR, "HTTP Sync Request Failed: " + e.getMessage(), MailBoxConstants.HTTPSYNCPROCESSOR, ExceptionUtils.getStackTrace(e));
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
        glassMessage.setCategory(HTTPSYNCPROCESSOR);
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

                    if (httpListenerProperties.containsKey(TTL_IN_SECONDS)) {
                        Integer ttlNumber = Integer.parseInt(httpListenerProperties.get(TTL_IN_SECONDS));
                        workTicket.setTtlDays(MailBoxUtil.convertTTLIntoDays(TTL_UNIT_SECONDS, ttlNumber));
                    }

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
                        glassMessage.logProcessingStatus(StatusType.ERROR, "HTTP Async Request Failed: " + e.getMessage(), MailBoxConstants.HTTPASYNCPROCESSOR, ExceptionUtils.getStackTrace(e));
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
