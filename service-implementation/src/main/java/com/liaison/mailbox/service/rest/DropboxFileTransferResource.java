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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.dropbox.authenticator.util.DropboxAuthenticatorUtil;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dropbox.DropboxFileTransferService;
import com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.WorkTicketUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox processor configuration services.
 *
 * @author OFS
 */
@Path("dropbox/transferContent")
public class DropboxFileTransferResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(DropboxFileTransferResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	protected static final String CONFIGURATION_MAX_REQUEST_SIZE = "com.liaison.servicebroker.sync.max.request.size";

	public DropboxFileTransferResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	@POST
	@ApiOperation(value = "upload content to spectrum", notes = "update details of existing mailbox", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response uploadContentAsync(@Context final HttpServletRequest serviceRequest,
			@QueryParam(value = "transferProfileId") final String transferProfileId) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

				serviceCallCounter.incrementAndGet();

				LOG.debug("Entering into uploadContentAsyncToSpectrum service.");

				DropboxAuthAndGetManifestResponseDTO responseEntity;
				DropboxAuthenticationService authService = new DropboxAuthenticationService();
				DropboxFileTransferService fileTransferService = new DropboxFileTransferService();
				// to calculate elapsed times of each individual task in this rest call
				long actualStartTime = System.currentTimeMillis();
				long startTime = 0;
				long endTime = 0;

				try {

					// start time to calculate elapsed time for retrieving necessary details from headers
					startTime = System.currentTimeMillis();

					String fileName = null;
                    if (!MailBoxUtil.isEmpty(serviceRequest.getHeader(MailBoxConstants.UPLOAD_FILE_NAME))) {
                        
                        fileName = URLDecoder.decode(serviceRequest.getHeader(MailBoxConstants.UPLOAD_FILE_NAME),
                                StandardCharsets.UTF_8.displayName());
                    }

					// get login id and auth token from mailbox token
					String mailboxToken = serviceRequest.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN);
					String aclManifest = serviceRequest.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
					if(StringUtil.isNullOrEmptyAfterTrim(mailboxToken) || StringUtil.isNullOrEmptyAfterTrim(aclManifest)) {
						throw new MailBoxConfigurationServicesException(Messages.REQUEST_HEADER_PROPERTIES_MISSING, Response.Status.BAD_REQUEST);
					}
					String loginId = DropboxAuthenticatorUtil.getPartofToken(mailboxToken, MailBoxConstants.LOGIN_ID);
					String authenticationToken = DropboxAuthenticatorUtil.getPartofToken(mailboxToken,
							MailBoxConstants.UM_AUTH_TOKEN);

					// end time to calculate elapsed time for retrieving necessary details from headers
					endTime = System.currentTimeMillis();
					LOG.debug("Calculating elapsed time for retrieving necessary details from headers");
					MailBoxUtil.calculateElapsedTime(startTime, endTime);

					// constructing authenticate and get manifest request
					DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = DropboxAuthenticatorUtil
							.constructAuthenticationRequest(loginId, null, authenticationToken);

					// to calculate elapsed time for authentication
					startTime = System.currentTimeMillis();

					// authenticating
					String encryptedMbxToken = authService
							.isAccountAuthenticatedSuccessfully(dropboxAuthAndGetManifestRequestDTO);
					if (encryptedMbxToken == null) {
						LOG.error("Dropbox - user authentication failed");
						responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_FAILURE,
								Messages.FAILURE);
						return Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON)
								.entity(responseEntity).build();
					}

					// to calculate elapsed time for authentication
					endTime = System.currentTimeMillis();
					LOG.debug("Calculating elapsed time for authentication");
					MailBoxUtil.calculateElapsedTime(startTime, endTime);


					// to calculate elapsed time for getting manifest
					startTime = System.currentTimeMillis();

					// getting manifest
					GEMManifestResponse manifestResponse = authService
							.getManifestAfterAuthentication(dropboxAuthAndGetManifestRequestDTO);
					if (manifestResponse == null) {
						responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTH_AND_GET_ACL_FAILURE,
								Messages.FAILURE);
						return Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON)
								.entity(responseEntity).build();
					}

					// to calculate elapsed time for getting manifest
					endTime = System.currentTimeMillis();
					LOG.debug("Calculating elapsed time for getting manifest");
					MailBoxUtil.calculateElapsedTime(startTime, endTime);

					// to calculate elapsed time for work ticket creation
					startTime = System.currentTimeMillis();

					//creating work ticket
					WorkTicketUtil workTicketUtil = new WorkTicketUtil();
					WorkTicket workTicket = workTicketUtil.createWorkTicket(getRequestProperties(serviceRequest), getRequestHeaders(serviceRequest), "", null);
					workTicketUtil.copyRequestHeadersToWorkTicket(serviceRequest, workTicket);
					// to calculate elapsed time for getting manifest
					endTime = System.currentTimeMillis();
					LOG.debug("Calculating elapsed time for creating workticket");
					MailBoxUtil.calculateElapsedTime(startTime, endTime);

					// to calculate elapsed time for work ticket creation
					startTime = System.currentTimeMillis();

					// calling service to upload content to spectrum
					DropboxTransferContentResponseDTO dropboxContentTransferDTO = fileTransferService
							.transferFile(workTicket, serviceRequest.getInputStream(), transferProfileId,
									manifestResponse.getManifest(), fileName, loginId);

					// to calculate elapsed time for getting manifest
					endTime = System.currentTimeMillis();
					LOG.debug("TIME SPENT IN SERVICE LAYER -  RETRIVE PROCESSOR PROPS + POST TO SPECTRUM + BUILD WORK TICKET + POST TO QUEUE");
					MailBoxUtil.calculateElapsedTime(startTime, endTime);
					String responseBody = MailBoxUtil.marshalToJSON(dropboxContentTransferDTO);

					// response message construction
					ResponseBuilder builder = Response
							.ok()
							.header(MailBoxConstants.ACL_MANIFEST_HEADER, manifestResponse.getManifest())
							.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, manifestResponse.getSignature())
							.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID,
									manifestResponse.getPublicKeyGuid())
							.header(MailBoxConstants.DROPBOX_AUTH_TOKEN, encryptedMbxToken)
							.type(MediaType.APPLICATION_JSON).entity(responseBody).status(Response.Status.OK);

					// to calculate elapsed time for getting manifest
					endTime = System.currentTimeMillis();
					LOG.debug("TOTAL TIME TAKEN TO TRANSFER FILE {} IS {}",workTicket.getFileName(),endTime-actualStartTime);
					MailBoxUtil.calculateElapsedTime(actualStartTime, endTime);
					LOG.debug("Exit from uploadContentAsyncToSpectrum service.");

					return builder.build();
				} catch (MailBoxServicesException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				} catch (IOException | JAXBException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "DropboxFileTransferResource.uploadContentAsync()";

		// hand the delegate to the framework for calling
		try {
			return handleAuditedServiceRequest(serviceRequest, worker);
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
		DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();
		int maxRequestSize = config.getInt(CONFIGURATION_MAX_REQUEST_SIZE);

		if (contentLength > maxRequestSize) {
			throw new RuntimeException("Request has content length of " + contentLength
					+ " which exceeds the configured maximum size of " + maxRequestSize);
		}
	}

	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
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
