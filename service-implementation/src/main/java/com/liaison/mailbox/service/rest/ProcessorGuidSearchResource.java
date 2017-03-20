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
 * Class used to fetch the processor ID details by mailbox name and mailbox name.
 */
@AppConfigurationResource
@Path("config/mailbox/processor/name")
@Api(value = "config/mailbox/processor/name", description = "Fetch the processor id by using the given mailbox and processor name")
public class ProcessorGuidSearchResource extends AuditedResource {

    private static final String PROCESSOR_NAME = "processorName";

    private static final String MAILBOX_NAME = "mbxName";

    /**
     * REST method to get the processor guid by using the processor name.
     * 
     * @return Response Object
    */
    @GET
    @ApiOperation(value = "Retieve the processor id", notes = "Fetch the processor id by using the given mailbox and processor name", 
    position = 1,
    response = GetProcessorIdResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
    public Response getProcessorIdByNameAndMbxName(
            @Context final HttpServletRequest request,
            @ApiParam(name = PROCESSOR_NAME, value = PROCESSOR_NAME, required = true) @QueryParam(value = PROCESSOR_NAME) final String processorName,
            @ApiParam(name = MAILBOX_NAME, value = MAILBOX_NAME, required = false) @QueryParam(value = MAILBOX_NAME) final String mbxName) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {
                ProcessorConfigurationService service = new ProcessorConfigurationService();
                return service.getProcessorIdByProcNameAndMbxName(mbxName, processorName);
            }
        };
        worker.actionLabel = "ProcessorGuidSearchResource.getProcessorIdByNameAndMbxName()";
        worker.queryParams.put(PROCESSOR_NAME, processorName);
        worker.queryParams.put(MAILBOX_NAME, mbxName);

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
