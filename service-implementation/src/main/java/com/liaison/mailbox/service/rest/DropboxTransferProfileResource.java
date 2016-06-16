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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dropbox.DropboxFileTransferService;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
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

	@GET
	@ApiOperation(value = "get list of transferprofiles", notes = "retrieve the list of transferprofiles", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response getTransferProfiles(@Context final HttpServletRequest serviceRequest) {
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call()
					throws Exception {

				DropboxAuthAndGetManifestResponseDTO responseEntity = null;
				DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = null;
				DropboxAuthenticationService authService = new DropboxAuthenticationService();
				DropboxFileTransferService fileTransferService = new DropboxFileTransferService();

				try {

					// get login id and auth token from mailbox token
					String loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);
					String authenticationToken = serviceRequest.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN);
					String aclManifest = serviceRequest.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
					dropboxMandatoryValidation(loginId, authenticationToken, aclManifest);

					// constructing authenticate and get manifest request
					dropboxAuthAndGetManifestRequestDTO = new DropboxAuthAndGetManifestRequestDTO(loginId, null, authenticationToken);

					// authentication
					String encryptedMbxToken = authService.isAccountAuthenticatedSuccessfully(dropboxAuthAndGetManifestRequestDTO);
					if (encryptedMbxToken == null) {
						LOG.error("Dropbox - user authentication failed");
						responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_FAILURE, Messages.FAILURE);
						return Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
					}

					// getting manifest
					GEMManifestResponse manifestResponse = authService.getManifestAfterAuthentication(dropboxAuthAndGetManifestRequestDTO);
					if (manifestResponse == null) {
						responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTH_AND_GET_ACL_FAILURE, Messages.FAILURE);
						return Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
					}

					GetTransferProfilesResponseDTO getTransferProfilesResponseDTO = fileTransferService.getTransferProfiles(manifestResponse.getManifest());
					String responseBody = MailBoxUtil.marshalToJSON(getTransferProfilesResponseDTO);

					// response message construction
					ResponseBuilder builder = constructResponse(loginId, encryptedMbxToken, manifestResponse, responseBody);
					return builder.build();

				} catch (MailBoxServicesException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}

		};
		worker.actionLabel = "DropboxFileTransferResource.getTransferProfiles()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);

		// hand the delegate to the framework for calling
		return process(serviceRequest, worker);
	}

	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
				PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
	}

}
