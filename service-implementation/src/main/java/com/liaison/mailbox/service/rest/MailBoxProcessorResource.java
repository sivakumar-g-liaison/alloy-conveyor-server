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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox processor configuration services.
 * 
 * @author veerasamyn
 */
@AppConfigurationResource
@Path("config/mailbox/{mailboxid}/processor")
@Api(value = "config/mailbox/{mailboxid}/processor", description = "Gateway for the processor configuration services.")
public class MailBoxProcessorResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxProcessorResource.class);

	/**
	 * REST method to update a processor.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @param guid The id of the mailbox
	 * 
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Processor", notes = "create a new processor", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Create new processor", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.AddProcessorToMailBoxRequest", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response createProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailboxid") final String guid,
			@QueryParam(value = "sid") @ApiParam(name = "sid", required = true, value = "Service instance id") final String serviceInstanceId) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				String requestString;
				try {
					requestString = getRequestBody(request);
					AddProcessorToMailboxRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							AddProcessorToMailboxRequestDTO.class);
					// create the new Processor
					ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
					final String userId = getUserIdFromHeader(request);
					return mailbox.createProcessor(guid, serviceRequest, serviceInstanceId, userId);

				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "MailBoxProcessorResource.createProcessor()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, guid);
        worker.queryParams.put(AuditedResource.SIID, serviceInstanceId);

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
