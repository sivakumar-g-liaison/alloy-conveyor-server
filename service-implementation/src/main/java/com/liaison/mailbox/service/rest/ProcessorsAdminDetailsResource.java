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

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.UpdateProcessorsExecutionStateRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorsExecutionStateResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/processorsadmin/processors/status")
@Api(value = "config/mailbox/processorsadmin/processors/status", description = "Change the processors status to failed")
public class ProcessorsAdminDetailsResource extends AuditedResource {
    
    /**
     * REST service to update the list of processors status
     * 
     * @param request guids
     * @return serviceResponse
     */
    @PUT
    @ApiOperation(value = "Update Processors Status to Failed", notes = "update existing processors status to failed",
            position = 1, response = com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorsExecutionStateResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service Failure.")})
    public Response updateProcessorStatusToFailed(
            @Context final HttpServletRequest request) {
        
        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            
            @Override
            public Object call() {

                String requestString;
                try {
                    
                    requestString = getRequestBody(request);
                    UpdateProcessorsExecutionStateRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
                            UpdateProcessorsExecutionStateRequestDTO.class);

                    ProcessorExecutionConfigurationService configService = new ProcessorExecutionConfigurationService();
                    final String userId = getUserIdFromHeader(request);
                    UpdateProcessorsExecutionStateResponseDTO serviceResponse = configService.updateExecutingProcessors(serviceRequest.getGuids(), userId);
                    return serviceResponse;
                } catch (IOException e) {
                    throw new LiaisonRuntimeException("Unable to Update the Processors Status to Failed " + e.getMessage(), e);
                }
            }
        };
        worker.actionLabel = "UpdateProcessorsExecStatusToFailedResource.updateProcessorsStatusToFailed()";

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

