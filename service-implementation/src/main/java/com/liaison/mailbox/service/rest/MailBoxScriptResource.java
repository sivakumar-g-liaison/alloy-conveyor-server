/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.util.StreamUtil;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ScriptService;
import com.liaison.mailbox.service.dto.configuration.request.ScriptServiceRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.GitlabCommitResponse;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the script file services.
 * 
 * @author OFS
 */

@AppConfigurationResource
@Path("config/mailbox/commithistory")
@Api(value = "config/mailbox/commithistory", description = "Read git commit by git file uri")
public class MailBoxScriptResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxScriptResource.class);

	/**
    *
    * Return Reads commit history from GitLab repository
    *
    * @param request
    * @param pguid
    * @return
    */
	@GET
	@ApiOperation(value = "Read git commit by git file uri",
    notes = "Reads commit history from gitLab repository",
    response = Response.class,
    position = 16)
	@Produces({ MediaType.TEXT_PLAIN })
	@ApiResponses( {
        @ApiResponse( code = 500, message = "Failed to fetch the commit history"),
        @ApiResponse( code = 404, message = "The script file was not found")
    })
	public Response readFileLastCommitHistory(@Context final HttpServletRequest request,
                                              @ApiParam(value = "URL where file is ", required = true)
                                              @QueryParam("url") final String url) throws Exception {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
	           @Override
	           public Object call() throws IOException {
	               
	               final String userId = getUserIdFromHeader(request);
	               String serviceIp = request.getLocalAddr();
	               
	               ScriptService scriptService = new ScriptService(serviceIp, userId);
	               
	               GitlabCommitResponse serviceResponse = scriptService.getFileLastCommitHistory(url);
	               return marshalResponse(serviceResponse.getStatus(), MediaType.APPLICATION_JSON, serviceResponse);
	           }
	       };
	    worker.actionLabel = "ScriptResource.readFileLastCommitHistory()";

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
