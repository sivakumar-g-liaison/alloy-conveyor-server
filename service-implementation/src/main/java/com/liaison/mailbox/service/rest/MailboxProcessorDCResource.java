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
import javax.ws.rs.PUT;
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
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorDCRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox processor DC services.
 *
 */
@AppConfigurationResource
@Path("config/mailbox/processordc")
@Api(value = "config/mailbox/processordc", description = "Gateway for the processor dc services.")
public class MailboxProcessorDCResource extends AuditedResource {

    /**
     * REST method to update existing processor DC.
     *
     * @param request HttpServletRequest, injected with context annotation
     * @return Response Object
     */
    @PUT
    @ApiOperation(value = "Update Processor DC", notes = "revise DC of valid processor", position = 4, response = com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO.class)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    public Response reviseProcessorDC(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

                String requestString;
                try {
                    requestString = getRequestBody(request);
                    ReviseProcessorDCRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
                            ReviseProcessorDCRequestDTO.class);
                    // updates existing processor dc
                    ProcessorConfigurationService service = new ProcessorConfigurationService();
                    final String userId = getUserIdFromHeader(request);
                    return service.reviseProcessorDC(serviceRequest,userId);

                } catch (IOException e) {
                    throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage(), e);
                }

            }
        };
        worker.actionLabel = "MailboxProcessorDCResource.reviseProcessorDC()";

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
