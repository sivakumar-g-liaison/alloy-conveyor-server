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

/**
 * Class used to fetch the list of Script URI from Processor.
 */

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorScriptService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/script")
@Api(value = "config/mailbox/script", description = "List the unique scripts URI")
public class ProcessorScriptListResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(ProcessorScriptListResource.class);

    /**
     * REST service to get all the ScriptURI
     *
     * @param request input request
     * @return Response search response
     */
    @GET
    @ApiOperation(value = "Get All the script", notes = "Get All the script", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.ProcessorScriptResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
    public Response listScriptURI(@Context HttpServletRequest request,
            @QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
            @QueryParam(value = "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") final String sortDirection,
            @QueryParam(value = "pageSize") @ApiParam(name = "pageSize", required = false, value = "pageSize") final String pageSize,
            @QueryParam(value = "scriptName") @ApiParam(name = "scriptName", required = false, value = "scriptName") final String scriptName) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {

                LOG.debug("Entering into listScriptURI service.");
                ProcessorScriptService processor = new ProcessorScriptService();
                GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
                searchFilter.setPage(page);
                searchFilter.setSortDirection(sortDirection);
                searchFilter.setPageSize(pageSize);
                searchFilter.setScriptName(scriptName);

                return processor.getListOfScriptURI(searchFilter);
            }
        };
        worker.actionLabel = "ProcessorScriptListResource.listScriptURI()";
        worker.queryParams.put(AuditedResource.SCRIPT_NAME, scriptName);

        // hand the delegate to the framework for calling
        return process(request, worker);
    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5, PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD, HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv, HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

}