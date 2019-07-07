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

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.MailboxStagedFileService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@AppConfigurationResource
@Path("config/mailbox/stagedFiles")
@Api(value = "config/mailbox/stagedFiles", description = "Gateway for the mailbox staged file services.")
public class MailboxStagedFileResource extends AuditedResource {

    /**
     * REST method to retrieve all staged files.
     * 
     * @return Response Object
    */
    @GET
    @ApiOperation(value = "List staged files", notes = "returns detail information of all the staged files", 
    position = 1, 
    response = com.liaison.mailbox.service.dto.dropbox.response.GetStagedFilesResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
    public Response readStagedFiles(
            @Context final HttpServletRequest request,
            @ApiParam(value = "Page Number", required = false) @QueryParam(value = "page") final String page,
            @ApiParam(value = "Page Size", required = false) @QueryParam(value = "pageSize") final String pageSize,
            @ApiParam(value = "Sorting Information", required = false) @QueryParam(value = "sortInfo") final String sortInfo,
            @ApiParam(value = "Filter Text", required = false) @QueryParam(value = "filterText") final String filterText) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {
                MailboxStagedFileService stagedFileService = new MailboxStagedFileService();
                return stagedFileService.getStagedFiles(page, pageSize, sortInfo, filterText);
            }
        };
        worker.actionLabel = "MailboxStagedFileResource.readStagedFiles()";
        worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);
        worker.queryParams.put("filterText", filterText);

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
