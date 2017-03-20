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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorIdResponseDTO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Class used to fetch the processor ID details.
 */
@AppConfigurationResource
@Path("config/mailbox/processor")
@Api(value = "config/mailbox/processor", description = "Fetch the processor id by using the given processor name")
public class ProcessorNameSearchResource extends AuditedResource {

    private static final String PGUID = "pguid";

    /**
     * REST method to get the processor guids by using the processor name.
     * 
     * @return Response Object
    */
    @GET
    @ApiOperation(value = "Retieve the processor name", notes = "Fetch the processor name by using the given processor pguid", 
    position = 1,
    response = GetProcessorIdResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
    public Response getProcessorNameByPguid(
            @Context final HttpServletRequest request,
            @ApiParam(name = PGUID, value = PGUID, required = true) @QueryParam(value = PGUID) final String pguid) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {
                ProcessorConfigurationService service = new ProcessorConfigurationService();
                return service.getProcessorNameByPguid(pguid);
            }
        };
        worker.actionLabel = "ProcessorNameSearchResource.getProcessorNameByPguid()";
        worker.queryParams.put(PGUID, pguid);

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
