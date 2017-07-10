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
import javax.ws.rs.DELETE;
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
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dropbox.DropboxUploadedFileService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.dropbox.UploadedFileDTO;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.response.GetUploadedFilesResponseDTO;
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
 * This is the gateway for the Dropbox uploaded files configuration services.
 *
 * @author OFS
 */
@AppConfigurationResource
@Path("config/dropbox/uploadedFiles")
@Api(value = "config/dropbox/uploadedFiles", description = "Gateway for the dropbox services.")
public class DropboxUploadedFileResource extends AuditedResource {

	private static final Logger LOGGER = LogManager.getLogger(DropboxUploadedFileResource.class);
	
	/**
	 * REST method to add uploaded file.
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 *
	 * @return Response Object
	 */
	@POST	
	@ApiOperation(value = "Added uploaded file", notes = "Add a new upload file entry", position = 1)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Add a new upload file entry")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response addUploadedFile(@Context final HttpServletRequest serviceRequest) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

			    LOGGER.debug("Entering into addUploadedFile service.");
               
                DropboxAuthAndGetManifestResponseDTO responseEntity = null;
                DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = null;
                DropboxAuthenticationService authService = new DropboxAuthenticationService();
                DropboxUploadedFileService uploadedFileService = new DropboxUploadedFileService();
				String requestString;
				
				try {
				    
				    // get login id and auth token from mailbox token
                    String authenticationToken = serviceRequest.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN);
                    String loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);
                    String aclManifest = serviceRequest.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
                    dropboxMandatoryValidation(loginId, authenticationToken, aclManifest);
                    
					requestString = getRequestBody(serviceRequest);
					UploadedFileDTO serviceRequest = JAXBUtility.unmarshalFromJSON(requestString,
							UploadedFileDTO.class);
					
					// constructing authenticate and get manifest request
                    dropboxAuthAndGetManifestRequestDTO = new DropboxAuthAndGetManifestRequestDTO(loginId, null, authenticationToken);
                    
                    // authentication
                    String encryptedMbxToken = authService.isAccountAuthenticatedSuccessfully(dropboxAuthAndGetManifestRequestDTO);
                    if (encryptedMbxToken == null) {
                        LOGGER.error("Dropbox - user authentication failed");
                        responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_FAILURE, Messages.FAILURE);
                        return Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
                    }

                    // getting manifest
                    GEMManifestResponse manifestResponse = authService.getManifestAfterAuthentication(dropboxAuthAndGetManifestRequestDTO);
                    if (manifestResponse == null) {
                        responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTH_AND_GET_ACL_FAILURE, Messages.FAILURE);
                        return Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
                    }
                    
					//add uploaded file
                    serviceRequest.setId(loginId);
					uploadedFileService.addUploadedFile(serviceRequest, true);					
					ResponseBuilder builder = constructResponse(loginId, encryptedMbxToken, manifestResponse, "Successfully added uploaded file");
					
                    LOGGER.debug("Exit from addUploadedFile service.");

                    return builder.build();
					
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				}
			}
		};
		worker.actionLabel = "DropboxUploadedFileResource.addUploadedFile()";

		// hand the delegate to the framework for calling
		return process(serviceRequest, worker);
	}
	
	
	@GET
	@ApiOperation(value = "get list of uploaded files", notes = "retrieve the list of uploaded files",
	position = 2, 
	response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response getUploadedFiles(
			@Context final HttpServletRequest serviceRequest,
			@QueryParam(value = "fileName") @ApiParam(name = "fileName", required = false, value = "Name of the staged file searched.") final String stageFileName,
			@QueryParam(value = "hitCounter") @ApiParam(name = "hitCounter", required = false, value = "hitCounter") final String hitCounter,
			@QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
			@QueryParam(value = "pageSize") @ApiParam(name = "pagesize", required = false, value = "pagesize") final String pageSize,
			@QueryParam(value = "sortField") @ApiParam(name = "sortField", required = false, value = "sortField") final String sortField,
			@QueryParam(value = "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") final String sortDirection) {
		
		// create the worker delegate to perform the business logic
				AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
					
					@Override
					public Object call()
							throws Exception {

						LOGGER.debug("Entering into getUploadedFiles service.");

						DropboxAuthAndGetManifestResponseDTO responseEntity = null;
						DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = null;
						DropboxAuthenticationService authService = new DropboxAuthenticationService();
						DropboxUploadedFileService  uploadedFilesService = new DropboxUploadedFileService();
						
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
								LOGGER.error("Dropbox - user authentication failed");
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
							searchFilter.setPage(page);
							searchFilter.setPageSize(pageSize);
							searchFilter.setSortField(sortField);
							searchFilter.setSortDirection(sortDirection);
							
							GetUploadedFilesResponseDTO getUploadedFilesResponseDTO = uploadedFilesService.getuploadedFiles(
									searchFilter, loginId);
							String responseBody = MailBoxUtil.marshalToJSON(getUploadedFilesResponseDTO);
							
							ResponseBuilder builder = constructResponse(loginId, encryptedMbxToken, manifestResponse, responseBody);
							
							LOGGER.debug("Exit from getUploadedFiles service.");

							return builder.build();
							
						} catch (IOException | JAXBException e) {
							LOGGER.error(e.getMessage(), e);
							throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
						}
						
					}
					
				};
				return process(serviceRequest, worker);
	}
	
	/**
     * REST method to delete uploaded file.
     *
     * @param request HttpServletRequest, injected with context annotation
     *
     * @return Response Object
     */
	@DELETE
    @ApiOperation(value = "delete specific uploaded file", notes = "delete specific uploaded file bu id", position = 2)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    public Response deleteUploadedFile(@Context final HttpServletRequest serviceRequest, 
                    @QueryParam(value = "uploadedFileId") @ApiParam(name = "uploadedFileId", required = true, value = "uploaded file id") final String uploadedFileId) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws Exception {
                DropboxAuthenticationService authService = new DropboxAuthenticationService();
                DropboxUploadedFileService uploadedFileService = new DropboxUploadedFileService();
                DropboxAuthAndGetManifestResponseDTO responseEntity = null;
                DropboxAuthAndGetManifestRequestDTO dropboxAuthAndGetManifestRequestDTO = null;

                try {

                    // get login id and auth token from mailbox token
                    String loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);
                    String authenticationToken = serviceRequest.getHeader(MailBoxConstants.DROPBOX_AUTH_TOKEN);
                    String aclManifest = serviceRequest.getHeader(MailBoxConstants.ACL_MANIFEST_HEADER);
                    dropboxMandatoryValidation(loginId, authenticationToken, aclManifest);

                    // constructing authenticate and get manifest request
                    dropboxAuthAndGetManifestRequestDTO = new DropboxAuthAndGetManifestRequestDTO(loginId, null, authenticationToken);

                    // authentication
                    String encryptedMbxToken =
                            authService.isAccountAuthenticatedSuccessfully(dropboxAuthAndGetManifestRequestDTO);
                    if (encryptedMbxToken == null) {
                        LOGGER.error("Dropbox - user authentication failed");
                        responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_FAILURE, Messages.FAILURE);
                        return Response.status(401)
                                       .header("Content-Type", MediaType.APPLICATION_JSON)
                                       .entity(responseEntity)
                                       .build();
                    }

                    // getting manifest
                    GEMManifestResponse manifestResponse = authService.getManifestAfterAuthentication(dropboxAuthAndGetManifestRequestDTO);
                    if (manifestResponse == null) {
                        responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTH_AND_GET_ACL_FAILURE, Messages.FAILURE);
                        return Response.status(400)
                                       .header("Content-Type", MediaType.APPLICATION_JSON)
                                       .entity(responseEntity)
                                       .build();
                    }

                    //deleted uploaded file                    
                    uploadedFileService.deleteUploadedFile(uploadedFileId);
                    // response message construction
                    ResponseBuilder builder = constructResponse(loginId, encryptedMbxToken, manifestResponse, "Successfully deleted uploaded file");
                    return builder.build();

                } catch (MailBoxServicesException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new LiaisonRuntimeException(e.getMessage());
                }
            }
        };
        worker.actionLabel = "DropboxUploadedFileResource.deleteUploadedFile()";
        //Added the guid
        worker.queryParams.put(AuditedResource.HEADER_GUID, uploadedFileId);

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
