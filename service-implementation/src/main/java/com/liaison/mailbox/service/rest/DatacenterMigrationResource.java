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

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.MigrationService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the gateway for update process dc.
 */
@AppConfigurationResource
@Path("config/migrate")
@Api(value = "process/migrate", description = "Gateway to update the datacenter in AT4 UAT and PROD.")
public class DatacenterMigrationResource extends AuditedResource {

    private static final String OPTION = "option";

    @GET
    @ApiOperation(value = "update the process_dc", notes = "update the process_dc", position = 1)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @AccessDescriptor(skipFilter = true)
    public Response migrate(@Context final HttpServletRequest request,
                            @QueryParam(value = "option") @ApiParam(name = "option", required = true, value = "option to update the dc") final int option) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {

                // updates datacenter of processors
                new MigrationService().migrate(option == 0 ? 1 : option);
                return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
            }
        };
        worker.actionLabel = "DatacenterMigrationResource.migrate()";
        worker.queryParams.put(OPTION, String.valueOf(option == 0 ? 1 : option));

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
