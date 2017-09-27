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

import com.google.gson.Gson;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dropbox.DropboxFileTransferService;
import com.liaison.mailbox.service.dropbox.filter.ConveyorAuthZ;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;

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
    @ApiOperation(value = "get list of transfer profiles",
            notes = "retrieve the list of transferprofiles",
            position = 1, response = com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO.class)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @ConveyorAuthZ
    public Response getTransferProfiles(@Context final HttpServletRequest serviceRequest,
                                        @HeaderParam(MailBoxConstants.MANIFEST_DTO) String manifestJson,
                                        @HeaderParam(MailBoxConstants.UM_AUTH_TOKEN) String token) {
        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws Exception {

                try {

                    // get login id and auth token from mailbox token
                    String loginId = serviceRequest.getHeader(MailBoxConstants.DROPBOX_LOGIN_ID);

                    // getting manifest
                    GEMManifestResponse manifestResponse = new Gson().fromJson(manifestJson, GEMManifestResponse.class);
                    GetTransferProfilesResponseDTO getTransferProfilesResponseDTO = new DropboxFileTransferService().getTransferProfiles(manifestResponse.getManifest());
                    String responseBody = MailBoxUtil.marshalToJSON(getTransferProfilesResponseDTO);

                    // response message construction
                    ResponseBuilder builder = constructResponse(loginId, token, manifestResponse, responseBody);
                    return builder.build();

                } catch (IOException e) {
                    throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage(), e);
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
