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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

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
import com.liaison.mailbox.service.dropbox.DropboxStagedFilesService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox processor configuration services.
 *
 * @author OFS
 */
@AppConfigurationResource
@Path("config/dropbox/stagedFiles")
@Api(value = "config/dropbox/stagedFiles", description = "Gateway for the dropbox services.")
public class DropboxFileStagedResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(DropboxFileStagedResource.class);
	protected static final String CONFIGURATION_MAX_REQUEST_SIZE = "com.liaison.servicebroker.sync.max.request.size";

	/**
	 * REST method to add staged file.
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 *
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create a staged file", notes = "create a new staged file entry", position = 1, response = com.liaison.mailbox.service.dto.dropbox.response.StagePayloadResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Create new processor", required = true, dataType = "com.liaison.mailbox.service.dto.dropbox.request.StagePayloadRequestDTO", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response addStagedFile(@Context final HttpServletRequest request) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				String requestString;
				try {
					requestString = getRequestBody(request);
					StagePayloadRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							StagePayloadRequestDTO.class);
					//Added the quid from service Request
                    if (serviceRequest != null && serviceRequest.getStagedFile() != null) {
						queryParams.put(AuditedResource.HEADER_GUID, serviceRequest.getStagedFile().getId());
					}
					// create the new staged file
					DropboxStagedFilesService stagedFileService = new DropboxStagedFilesService();
					return stagedFileService.addStagedFile(serviceRequest, null);

				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "DropboxFileStagedResource.addStagedFile()";

		// hand the delegate to the framework for calling
		return process(request, worker);
	}

	@GET
	@ApiOperation(value = "get list of staged files", notes = "retrieve the list of staged files", position = 2, response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response getStagedFiles(
			@Context final HttpServletRequest serviceRequest,
			@QueryParam(value = "fileName") @ApiParam(name = "fileName", required = false, value = "Name of the staged file searched.") final String stageFileName,
			@QueryParam(value = "hitCounter") @ApiParam(name = "hitCounter", required = false, value = "hitCounter") final String hitCounter,
			@QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
			@QueryParam(value = "pageSize") @ApiParam(name = "pagesize", required = false, value = "pagesize") final String pageSize,
			@QueryParam(value = "sortField") @ApiParam(name = "sortField", required = false, value = "sortField") final String sortField,
			@QueryParam(value = "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") final String sortDirection,
			@QueryParam(value = "status") @ApiParam(name = "status", required = false, value = "Status of staged file") final String status) {
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call()
					throws Exception {

				LOG.debug("Entering into getStagedFiles service.");

				DropboxAuthAndGetManifestResponseDTO responseEntity = null;
				DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = null;
				DropboxAuthenticationService authService = new DropboxAuthenticationService();
				DropboxStagedFilesService fileStagedService = new DropboxStagedFilesService();

				try {

					// get login id and auth token from mailbox token
					String authenticationToken = serviceRequest.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN);
					String loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);
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

					// construct the generic search filter dto
					GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
					searchFilter.setStagedFileName(stageFileName);
					searchFilter.setPage(page);
					searchFilter.setPageSize(pageSize);
					searchFilter.setSortField(sortField);
					searchFilter.setSortDirection(sortDirection);
					searchFilter.setStatus(status);

					// getting staged files based on manifest
					GetStagedFilesResponseDTO getStagedFilesResponseDTO = fileStagedService.getStagedFiles(
							searchFilter, manifestResponse.getManifest());
					getStagedFilesResponseDTO.setHitCounter(hitCounter);
					String responseBody = MailBoxUtil.marshalToJSON(getStagedFilesResponseDTO);

					ResponseBuilder builder = constructResponse(loginId, encryptedMbxToken, manifestResponse, responseBody);
					LOG.debug("Exit from getStagedFiles service.");

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
		worker.actionLabel = "DropboxFileTransferResource.getStagedFiles()";
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
