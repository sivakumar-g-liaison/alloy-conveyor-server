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
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
import com.liaison.dropbox.authenticator.util.DropboxAuthenticatorUtil;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dropbox.DropboxFileTransferService;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox processor configuration services.
 * 
 * @author santoshc
 */
@AppConfigurationResource
@Path("config/dropbox/transferProfiles")
@Api(value = "config/dropbox/transferProfiles", description = "Gateway for the dropbox services.")
public class DropboxTransferProfileResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(DropboxTransferProfileResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public DropboxTransferProfileResource()
			throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	@GET
	@ApiOperation(value = "get list of transferprofiles", notes = "retrieve the list of transferprofiles", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response getTransferProfiles(@Context final HttpServletRequest serviceRequest) {
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call()
					throws Exception {

				serviceCallCounter.incrementAndGet();

				DropboxAuthAndGetManifestResponseDTO responseEntity;
				DropboxAuthenticationService authService = new DropboxAuthenticationService();
				DropboxFileTransferService fileTransferService = new DropboxFileTransferService();

				try {

					// get login id and auth token from mailbox token
					String mailboxToken = serviceRequest.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN);
					String aclManifest = serviceRequest.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
					if (StringUtil.isNullOrEmptyAfterTrim(mailboxToken)
							|| StringUtil.isNullOrEmptyAfterTrim(aclManifest)) {
						LOG.error("ACL manifest or mailbox token missing.");
						throw new MailBoxConfigurationServicesException(Messages.REQUEST_HEADER_PROPERTIES_MISSING,
								Response.Status.BAD_REQUEST);
					}
					String loginId = DropboxAuthenticatorUtil.getPartofToken(mailboxToken, MailBoxConstants.LOGIN_ID);
					String authenticationToken = DropboxAuthenticatorUtil.getPartofToken(mailboxToken,
							MailBoxConstants.UM_AUTH_TOKEN);

					// constructing authenticate and get manifest request
					DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = DropboxAuthenticatorUtil.constructAuthenticationRequest(
							loginId, null, authenticationToken);

					// authenticating
					String encryptedMbxToken = authService.isAccountAuthenticatedSuccessfully(dropboxAuthAndGetManifestRequestDTO);
					if (encryptedMbxToken == null) {
						LOG.error("Dropbox - user authentication failed");
						responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_FAILURE,
								Messages.FAILURE);
						return Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON).entity(
								responseEntity).build();
					}

					// getting manifest
					GEMManifestResponse manifestResponse = authService.getManifestAfterAuthentication(dropboxAuthAndGetManifestRequestDTO);
					if (manifestResponse == null) {
						LOG.error("Dropbox - authenticated but failed to retrieve manifest");
						responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTH_AND_GET_ACL_FAILURE,
								Messages.FAILURE);
						return Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON).entity(
								responseEntity).build();
					}

					GetTransferProfilesResponseDTO getTransferProfilesResponseDTO = fileTransferService.getTransferProfiles(manifestResponse.getManifest());
					String responseBody = MailBoxUtil.marshalToJSON(getTransferProfilesResponseDTO);

					// response message construction
					ResponseBuilder builder = Response
							.ok()
							.header(MailBoxConstants.ACL_MANIFEST_HEADER, manifestResponse.getManifest())
							.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, manifestResponse.getSignature())
							.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID,
									manifestResponse.getPublicKeyGuid())
							.header(MailBoxConstants.DROPBOX_AUTH_TOKEN, encryptedMbxToken)
							.type(MediaType.APPLICATION_JSON).entity(responseBody).status(Response.Status.OK);
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
		worker.actionLabel = "DropboxFileTransferResource.getTransferProfiles()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);

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
