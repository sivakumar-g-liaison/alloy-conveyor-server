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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the processor affinity support services.
 *
 */

@AppConfigurationResource
@Path("config/processoraffinity")
@Api(value = "config/processoraffinity", description = "Gateway for the processor affinity support services.")
public class MailBoxProcessorAffinityResource extends AuditedResource {

    /**
     * REST method support to the processoraffinity.
     *
     * @param request
     *            HttpServletRequest, injected with context annotation
     * @return Response Object
     */
    @POST
    @ApiOperation(value = "Support processor affinity", notes = "Support processor affinity", position = 1)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
    public Response supportProcessorAffinity(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {

                String requestString;
                try {
                    requestString = getRequestBody(request);
                    // updates datacenter of processors
                    ProcessorConfigurationService service = new ProcessorConfigurationService();
                    service.supportProcessorAffinity(requestString);
                    return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
                    
                } catch (IOException e) {
                    throw new LiaisonRuntimeException("Unable to Process Request. " + e.getMessage(), e);
                }

            }
        };
        worker.actionLabel = "MailBoxProcessorAffinityResource.supportProcessorAffinity()";

        // hand the delegate to the framework for calling
        return process(request, worker);
    }
    
    /**
     * REST method to retrieve a datacenter and corresponding processors details.
     *
     * @return Response Object
     */
    @GET
    @ApiOperation(value = "Datacenter Details", notes = "returns detail information of a Datacenter and correponding processors", position = 2, response = com.liaison.mailbox.service.dto.configuration.response.GetDatacenterResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    public Response getDatacenters(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {

                // Gets processor details.
                ProcessorConfigurationService service = new ProcessorConfigurationService();
                return service.getDatacenters();
            }
        };
        worker.actionLabel = "MailBoxProcessorAffinityResource.getDatacenters()";
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
