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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.InboundFileService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the create inbound file service.
 */
@AppConfigurationResource
@Path("config/mailbox/inbound")
@Api(value = "config/mailbox/inbound", description = "Gateway for the create inbound file service.")
public class InboundFileResource extends AuditedResource {

    /**
     * REST method to create inbound file.
     *
     * @param request HttpServletRequest, injected with context annotation
     * @return Response Object
     */

    @POST
    @ApiOperation(value = "Create Inbound File", notes = "create a new inbound file", position = 1)
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Create new inbound file", required = true, paramType = "body")})
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    public Response createInboundFile(@Context final HttpServletRequest request) {
        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

            @Override
            public Object call() {
                try {
                    return new InboundFileService().constructAndCreateInboundFile(getRequestBody(request));
                } catch (Exception e) {
                    return new LiaisonRuntimeException("Unable to Read Request. ", e);
                }
            }
        };

        worker.actionLabel = "InboundFileResource.createInboundFile()";

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
