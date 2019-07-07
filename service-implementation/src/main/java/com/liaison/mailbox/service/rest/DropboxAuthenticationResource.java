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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import com.liaison.mailbox.service.dropbox.DropboxAuthenticationService;
import com.liaison.mailbox.service.dto.dropbox.request.DropboxAuthAndGetManifestRequestDTO;
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
 * @author OFS
 */
@AppConfigurationResource
@Path("config/dropbox/authenticate")
@Api(value = "config/dropbox/authenticate", description = "Gateway for the dropbox services.")
public class DropboxAuthenticationResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(DropboxAuthenticationResource.class);

    /**
     * REST method to update a processor.
     *
     * @param request HttpServletRequest, injected with context annotation
     *
     * @return Response Object
     */
    @POST
    @ApiOperation(value = "Authenticate Account", notes = "authenticate an user account", position = 1, response = com.liaison.usermanagement.service.dto.response.AuthenticateUserAccountResponseDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Authenticate an user account", required = true, dataType = "com.liaison.usermanagement.swagger.dto.request.AuthenticateRequest", paramType = "body")})
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @AccessDescriptor(skipFilter = true)
    public Response authenticateAccount(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

            @Override
            public Object call() throws Exception {

                DropboxAuthAndGetManifestRequestDTO serviceRequest;

                try {

                    String requestString = getRequestBody(request);
                    serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, DropboxAuthAndGetManifestRequestDTO.class);
                    return new DropboxAuthenticationService().authenticateAccount(serviceRequest);
                } catch (IOException e) {
                    throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage(), e);
                }

            }

        };
        worker.actionLabel = "DropboxAuthenticationResource.authenticateAccount()";

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
