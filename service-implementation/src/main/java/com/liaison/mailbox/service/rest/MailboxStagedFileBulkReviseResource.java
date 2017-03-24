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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.MailboxStagedFileService;
import com.liaison.mailbox.service.dto.configuration.request.ReviseStagedFileRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This class is for staged files update services
 */
@AppConfigurationResource
@Path("config/mailbox/stagedFiles/revise")
@Api(value = "config/mailbox/stagedFiles/revise", description = "Gateway for the mailbox staged file bulk update services.")
public class MailboxStagedFileBulkReviseResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(MailboxStagedFileBulkReviseResource.class);

    /**
     * REST method to bulk update the staged files status to failed.
     * 
     * @return Response Object
    */
    @PUT
    @ApiOperation(value = "staged files status update", notes = "staged files status bulk update from staged to failed", 
    position = 1,
    response = com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
    public Response updateBulkStagedFileStatus(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws IOException {

                String requestString = getRequestBody(request);
                MailboxStagedFileService stagedFileService = new MailboxStagedFileService();
                ReviseStagedFileRequestDTO requestDTO = MailBoxUtil.unmarshalFromJSON(requestString, ReviseStagedFileRequestDTO.class);
                return stagedFileService.deactivateBulkStagedFiles(requestDTO);
            }
        };
        worker.actionLabel = "MailboxStagedFileBulkReviseResource.updateBulkStagedFileStatus()";
        worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);

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
