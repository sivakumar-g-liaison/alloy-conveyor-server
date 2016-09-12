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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
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
@Path("config/mailbox/{mailboxid}/processor/{processorid}")
@Api(value = "config/mailbox/{mailboxid}/processor/{processorid}", description = "Gateway for the processor configuration services.")
public class MailBoxProcessorDetailsResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxProcessorDetailsResource.class);

	/**
	 * REST method to remove a processor details.
	 * 
	 * @param mailboxguid The id of the mailbox
	 * @param guid The id of the processor
	 * @return Response Object
	 * 
	 */
	@DELETE
	@ApiOperation(value = "Remove Processor", notes = "remove processor details", position = 2, response = com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response deleteProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") final String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") final String guid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				// Deactivating processor
				ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
				final String userId = getUserIdFromHeader(request);
				return mailbox.deactivateProcessor(mailboxguid, guid, userId);
			}
		};
		worker.actionLabel = "MailBoxProcessorDetailsResource.deleteProcessor()";
		worker.queryParams.put("mailboxid", mailboxguid);
		worker.queryParams.put(AuditedResource.HEADER_GUID, guid);

		// hand the delegate to the framework for calling
		return process(request, worker);

	}

	/**
	 * REST method to retrieve a mailbox details.
	 * 
	 * @param mailboxguid The id of the mailbox
	 * @param guid The id of the processor
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Processor Details", notes = "returns detail information of a valid processor", position = 3, response = com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response getProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") final String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") final String guid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call()
					throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

				try {
					// Gets processor details.
					ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
					return mailbox.getProcessor(mailboxguid, guid);

				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailBoxProcessorDetailsResource.getProcessor()";
		worker.queryParams.put("mailboxid", mailboxguid);
		worker.queryParams.put(AuditedResource.HEADER_GUID, guid);

		// hand the delegate to the framework for calling
		return process(request, worker);

	}

	/**
	 * REST method to update existing processor.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @param mailboxguid The id of the mailbox
	 * @param guid The id of the processor
	 * @return Response Object
	 */
	@PUT
	@ApiOperation(value = "Update Processor", notes = "revise details of valid processor", position = 4, response = com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Update  processor", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.ReviseProcessorRequest", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response reviseProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") final String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") final String guid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call()
					throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

				String requestString;
				try {
					requestString = getRequestBody(request);
					ReviseProcessorRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							ReviseProcessorRequestDTO.class);
					// updates existing processor
					ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
					final String userId = getUserIdFromHeader(request);
					return mailbox.reviseProcessor(serviceRequest, mailboxguid, guid, userId);

				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailBoxProcessorDetailsResource.getProcessor()";
		worker.queryParams.put("mailboxid", mailboxguid);
		worker.queryParams.put(AuditedResource.HEADER_GUID, guid);

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
