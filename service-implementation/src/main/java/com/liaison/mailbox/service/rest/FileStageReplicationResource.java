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
import com.liaison.commons.util.StreamUtil;
import com.liaison.framework.RuntimeProcessResource;
import com.liaison.mailbox.service.core.FileStageReplicationService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the gateway for trigger profile services.
 *
 * @author OFS
 */
@RuntimeProcessResource
@Path("process/mailbox/replication")
@Api(value = "config/mailbox/replication", description = "Trigger profile services")
public class FileStageReplicationResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(FileStageReplicationResource.class);

    /**
     * REST method to trigger a profile and run the processors in that profile.
     *
     * @return Response Object
     */
    @POST
    @ApiOperation(value = "Stage File Replication REST", notes = "Stage a file", position = 23, response = java.lang.String.class)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @AccessDescriptor(skipFilter = true)
    public Response replicateFile(@Context final HttpServletRequest request) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws Exception {

                LOG.debug("Entering into trigger profile resource.");
                String msg = new String(StreamUtil.streamToBytes(request.getInputStream()));
                FileStageReplicationService service = new FileStageReplicationService(msg);
                service.stage(msg);

                return Response.ok("Staged Successfully").build();
            }
        };

        worker.actionLabel = "TriggerProfileResource.triggerProfile()";
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
