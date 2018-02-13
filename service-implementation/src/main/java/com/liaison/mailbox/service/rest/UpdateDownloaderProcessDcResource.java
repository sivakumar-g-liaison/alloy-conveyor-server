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
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the profile configuration services.
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("config/downloader/updateprocessdc")
@Api(value = "config/downloader/updateprocessdc", description = "Gateway for the update the process_dc.")
public class UpdateDownloaderProcessDcResource extends AuditedResource {

	/**
	 * REST method to update the process_dc column of PROCESSOR table
	 * 
	 * @param request
	 * @return
	 */
	@PUT
	@ApiOperation(value = "update the process_dc", notes = "update the process_dc", position = 1)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response updateProcessDc(@Context final HttpServletRequest request,
	        @QueryParam(value = "current_dc") final @ApiParam(name = "current_dc", required = true, value = "Existing dc") String current_dc,
	        @QueryParam(value = "updated_dc") final @ApiParam(name = "updated_dc", required = true, value = "DC to update") String updated_dc) {
	    
	    // create the worker delegate to perform the business logic
	    AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
	        @Override
	        public Object call() {
	            
                // updates datacenter of downloder processor
	            // TODO processing_dc values from Rest request body
                new ProcessorConfigurationService().updateDownloaderProcessDc(current_dc, updated_dc);
                new StagedFileDAOBase().updateStagedFileProcessDC(current_dc, updated_dc);
                return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
	        }
	    };
	    worker.actionLabel = "UpdateDownloaderProcessDcResource.updateProcessDc()";
	    
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
