/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
import com.liaison.mailbox.service.dto.dropbox.response.DropboxAuthAndGetManifestResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox processor configuration services.
 *
 * @author santoshc
 */
@AppConfigurationResource
@Path("config/dropbox/authAndGetACL")
@Api(value = "config/dropbox/authAndGetACL", description = "Gateway for the dropbox manifest services.")
public class DropboxManifestResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(DropboxManifestResource.class);

    /**
     * REST method to authenticate and retrieve manifest details.
     *
     * @param request
     * @return Response
     */
    @POST
    @ApiOperation(value = "Get manifest", notes = "This function is used to authencate from usermanagement and get manifest from gem", position = 3, response = com.liaison.gem.service.dto.response.GetManifestResponseDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "authenticateAndGetManifest", required = true, dataType = "com.liaison.usermanagement.swagger.dto.request.AuthenticateRequest", paramType = "body")})
    @ApiResponses({
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 403, message = "Permission Denied."),
            @ApiResponse(code = 500, message = "Unexpected Service failure.")
    })
    @AccessDescriptor(skipFilter = true)
    public Response authenticateAndGetManifest(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws IOException {

                LOG.debug("Entering into authenticate and get manifest service");
                DropboxAuthAndGetManifestRequestDTO serviceRequest = null;
                DropboxAuthAndGetManifestResponseDTO responseEntity = null;

                try {

                    String requestString = getRequestBody(request);
                    serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, DropboxAuthAndGetManifestRequestDTO.class);
                } catch (IOException e) {
                    throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage(), e);
                }

                DropboxAuthenticationService dropboxService = new DropboxAuthenticationService();

                // authentication
                String loginId = serviceRequest.getLoginId();
                String encryptedMbxToken = dropboxService.isAccountAuthenticatedSuccessfully(serviceRequest);
                if (encryptedMbxToken == null) {
                    LOG.error("Dropbox - user authentication failed");
                    responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTHENTICATION_FAILURE, Messages.FAILURE);
                    return Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
                }

                // getting manifest
                GEMManifestResponse manifestResponse = dropboxService.getManifestAfterAuthentication(serviceRequest);
                if (manifestResponse == null) {
                    LOG.error("Dropbox - user authenticated but failed to retrieve manifest.");
                    responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.AUTH_AND_GET_ACL_FAILURE, Messages.FAILURE);
                    return Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON).entity(responseEntity).build();
                }

                responseEntity = new DropboxAuthAndGetManifestResponseDTO(Messages.USER_AUTHENTICATED_AND_GET_MANIFEST_SUCCESSFUL, Messages.SUCCESS);
                ResponseBuilder builder = constructResponse(loginId, encryptedMbxToken, manifestResponse, MailBoxUtil.marshalToJSON(responseEntity));
                LOG.debug("Exit from authenticate and get manifest service.");

                return builder.build();
            }
        };
        worker.actionLabel = "DropboxManifestResource.authenticateAndGetManifest()";

        // hand the delegate to the framework for calling
        return process(request, worker);
    }

    /**
     * REST method to retrieve manifest details.
     *
     * @param request
     * @return Response
     */
    @GET
    @ApiOperation(value = "Get manifest", notes = "This function is used to get manifest from gem", position = 3, response = com.liaison.gem.service.dto.response.GetManifestResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @AccessDescriptor(skipFilter = true)
    public Response getManifest(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws IOException, MessagingException {
                return new DropboxAuthenticationService().getManifest();
            }
        };
        worker.actionLabel = "DropboxManifestResource.getManifest()";

        // hand the delegate to the framework for calling
        return process(request, worker);
    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
                PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

}
