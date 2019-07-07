/**
 * Copyright 2014 Liaison Technologies, Inc.
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ScriptService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the script file details services.
 * 
 * @author OFS
 */

@AppConfigurationResource
@Path("config/mailbox/git/content/{git.file.name}")
@Api(value = "config/mailbox/git/content/{git.file.name}", description = "Gateway for the script file details services")
public class MailBoxScriptDetailsConfigurationResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxScriptDetailsConfigurationResource.class);

	/**
	 * 
	 * Rest method to fetch script file from git
	 * 
	 * @param request
	 * @param pguid
	 * @return Response
	 */
	@GET
	@ApiOperation(value = "fetch the script file by url", notes = "The script file are loaded from GitLab repository", position = 3, response = com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "fetch script file", required = true, dataType = "com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response readScript(
			@Context final HttpServletRequest request,
			@PathParam(value = "git.file.name") @ApiParam(name = "git.file.name", required = true, value = "URL where file is going to be fetch") final String gitFileName)
			throws Exception {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				try {

					ScriptService scriptService = new ScriptService(null, null);
					return scriptService.getScript(gitFileName, "");
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailBoxScriptDetailsConfigurationResource.readScript()";
		worker.queryParams.put("gitFileName", gitFileName);

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
