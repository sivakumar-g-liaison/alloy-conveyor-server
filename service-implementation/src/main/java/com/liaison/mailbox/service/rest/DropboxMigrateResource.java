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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.dropbox.DropboxUploadedFileService;
import com.liaison.spectrum.client.model.table.DataTableRow;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the Dropbox data migrate configuration services.
 *
 * @author OFS
 */
@AppConfigurationResource
@Path("config/dropbox/migrate")
@Api(value = "config/dropbox/migrate", description = "Gateway for the dropbox services.")
public class DropboxMigrateResource extends AuditedResource {

    private static final Logger LOG = LogManager.getLogger(DropboxMigrateResource.class);

    /**
     * REST method to migrate of uploaded files and it will be removed in next release
     *
     * @param serviceRequest HttpServletRequest
     *
     * @return Response Object
     */
    @POST
    @ApiOperation(value = "migrate uploaded files", notes = "migrate uploaded files entry", position = 1)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "migrate uploaded files entry")})
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @AccessDescriptor(skipFilter = true)
    public Response migrateUploadedHistory(@Context final HttpServletRequest serviceRequest) {

        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {

                LOG.debug("Entering into migrateUploadedHistory service.");

                DropboxUploadedFileService uploadedFileService = new DropboxUploadedFileService();
                String requestString;

                try {

                    requestString = getRequestBody(serviceRequest);
                    DataTableRow[] serviceRequest = JAXBUtility.unmarshalFromJSON(requestString, DataTableRow[].class);
                    //Migrate uploaded files
                    uploadedFileService.migrateUploadedHistory(serviceRequest);
                    ResponseBuilder builder = Response.ok().entity("Successfully done the data migration for uploaded files").status(Response.Status.OK);

                    LOG.debug("Exit from migrateUploadedHistory service.");
                    return builder.build();

                } catch (Exception e) {
                    throw new LiaisonRuntimeException(e.getMessage(), e);
                }
            }
        };
        worker.actionLabel = "DropboxMigrateResource.migrateUploadedHistory()";

        // hand the delegate to the framework for calling
        return process(serviceRequest, worker);
    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
                PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

}
