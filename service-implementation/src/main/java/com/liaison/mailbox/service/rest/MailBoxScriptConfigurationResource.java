/**
 * Copyright 2018 Liaison Technologies, Inc.
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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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
import com.liaison.commons.util.StreamUtil;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ScriptService;
import com.liaison.mailbox.service.dto.configuration.request.ScriptServiceRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the script file services.
 * 
 * @author OFS
 */

@AppConfigurationResource
@Path("config/mailbox/git/content")
@Api(value = "config/mailbox/git/content", description = "Gateway for the user script services")
public class MailBoxScriptConfigurationResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxScriptConfigurationResource.class);

	/**
	 * Rest method to create a script file to git
	 * 
	 * @param request
	 * @return Response
	 * @throws Exception
	 */
	@POST
	@ApiOperation(value = "Create a new script file to GitLab", notes = "This function creates a new script file in GitLab", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "create a new script", required = true, dataType = "com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response createScript(@Context final HttpServletRequest request)
			throws Exception {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

			@Override
			public Object call()
					throws Exception {

				ScriptServiceRequestDTO serviceRequest;
				final String userId = getUserIdFromHeader(request);
				String serviceIp = request.getLocalAddr();
				ScriptService scriptService = new ScriptService(serviceIp, userId);
				try (InputStream requestStream = request.getInputStream()) {

					String requestString = new String(StreamUtil.streamToBytes(requestStream), "UTF-8");
					serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, ScriptServiceRequestDTO.class);
					return scriptService.createScript(serviceRequest.getScript());

				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailBoxScriptConfigurationResource.createScript()";

		// hand the delegate to the framework for calling
		return process(request, worker);
	}

	/**
	 * Rest method to revise a script file to git
	 * 
	 * @param request
	 * @return Response
	 * @throws Exception
	 */
	@PUT
	@ApiOperation(value = "update a new script file to GitLab", notes = "This function update a new script file in GitLab", position = 2, response = com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "update a new script", required = true, dataType = "com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response updateScript(@Context final HttpServletRequest request)
			throws Exception {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

			@Override
			public Object call()
					throws Exception {

				ScriptServiceRequestDTO serviceRequest;
				final String userId = getUserIdFromHeader(request);
				String serviceIp = request.getLocalAddr();
				ScriptService scriptService = new ScriptService(serviceIp, userId);
				try (InputStream requestStream = request.getInputStream()) {

					String requestString = new String(StreamUtil.streamToBytes(requestStream), "UTF-8");
					serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, ScriptServiceRequestDTO.class);
					return scriptService.updateScript(serviceRequest.getScript());

				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailBoxScriptConfigurationResource.updateScript()";

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
