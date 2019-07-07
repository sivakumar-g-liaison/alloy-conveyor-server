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
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for read cluster type of mailbox/processor.
 */
@AppConfigurationResource
@Path("config/clustertype")
@Api(value = "config/clustertype", description = "Gateway for cluster type read service.")
public class ClusterTypeResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(ClusterTypeResource.class);


    @GET
    @ApiOperation(value = "read clusterType", notes = "returns cluster type of mailbox/processor", position = 3,
            response = com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    public Response readClusterType(
            @Context final HttpServletRequest request,
            @QueryParam(value = TYPE) final @ApiParam(name = TYPE, required = true, value = TYPE) String type,
            @QueryParam(value = "pguid") final @ApiParam(name = "pguid", required = true, value = "pguid") String pguid) {

        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

            @Override
            public Object call() {

                LOG.info("The requested type is {}", type);
                switch (type.toLowerCase()) {

                    case MailBoxConstants.TYPE_PROCESSOR:
                        return new ProcessorConfigurationService().getClusterType(pguid);
                    case MailBoxConstants.TYPE_MAILBOX:
                        return new MailBoxConfigurationService().getClusterType(pguid);
                    default:
                        return marshalResponse(Response.Status.BAD_REQUEST.getStatusCode(), MediaType.TEXT_PLAIN, "Unsupported type");
                }
            }
        };
        worker.actionLabel = "ClusterTypeResource.readClusterType()";
        worker.queryParams.put(TYPE, type);
        worker.queryParams.put(AuditedResource.HEADER_GUID, pguid);

        // hand the delegate to the framework for calling
        return process(request, worker);

    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(AuditStatement.Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
                PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

}
