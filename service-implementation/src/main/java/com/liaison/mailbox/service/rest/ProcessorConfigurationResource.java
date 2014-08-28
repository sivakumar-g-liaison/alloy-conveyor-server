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
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.util.StreamUtil;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
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
@Path("mailbox/{mailboxid}/processor")
@Api(value = "mailbox/{mailboxid}/processor", 
description = "Gateway for the processor configuration services.")
public class ProcessorConfigurationResource extends AuditedResource {
	
	private static final Logger LOG = LogManager.getLogger(ProcessorConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public ProcessorConfigurationResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST method to update a processor.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @param guid
	 *            The id of the mailbox
	 * 
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Processor",
	notes = "create a new processor",
	position = 1,
	response = com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Create new processor", required = true,
			dataType = "com.liaison.mailbox.swagger.dto.request.AddProcessorToMailBoxRequest", paramType = "body") })
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "createProcessor")
	public Response createProcessor(
			@Context HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name="mailboxid", required=true, value="mailboxid") String guid,
			@QueryParam(value = "sid") @ApiParam(name = "sid", required = true, value = "Service instance id") String serviceInstanceId) {

		// Audit LOG the Attempt to createProcessor
		auditAttempt("createProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		AddProcessorToMailboxRequestDTO serviceRequest;

		try (InputStream requestStream = request.getInputStream()) {

			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, AddProcessorToMailboxRequestDTO.class);

			// add the new profile details
			AddProcessorToMailboxResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();

			serviceResponse = mailbox.createProcessor(guid, serviceRequest, serviceInstanceId);

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "createProcessor");

			// populate the response body
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "processorConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("createProcessor");
		}
		return returnResponse;

	}
	
	/**
	 * REST method to remove a processor details.
	 * 
	 * @param mailboxguid
	 *            The id of the mailbox
	 * @param guid
	 *            The id of the processor
	 * @return Response Object
	 * 
	 */
	@DELETE
	@ApiOperation(value = "Remove Processor",
			notes = "remove processor details",
			position = 2,
			response = com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO.class)
	@Path("/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "deleteProcessor")
	public Response deleteProcessor(
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") String guid) {

		// Audit LOG the Attempt to deleteProcessor
		auditAttempt("deleteProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			// add the new profile details
			DeActivateProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Deactivating processor
			serviceResponse = mailbox.deactivateProcessor(mailboxguid, guid);

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "deleteProcessor");

			// Constructing response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProcessorConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("deleteProcessor");
		}
		return returnResponse;

	}
	
	/**
	 * REST method to retrieve a mailbox details.
	 * 
	 * @param mailboxguid
	 *            The id of the mailbox
	 * @param guid
	 *            The id of the processor
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Processor Details",
			notes = "returns detail information of a valid processor",
			position = 3,
			response = com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO.class)
	@Path("/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "getProcessor")
	public Response getProcessor(
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") String guid) {

		// Audit LOG the Attempt to getProcessor
		auditAttempt("getProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			GetProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Gets processor details.
			serviceResponse = mailbox.getProcessor(mailboxguid, guid);

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "getProcessor");

			// constructs response.
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "Get Processor failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("getProcessor");
		}
		return returnResponse;

	}
	
	/**
	 * REST method to update existing processor.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @param mailboxguid
	 *            The id of the mailbox
	 * @param guid
	 *            The id of the processor
	 * @return Response Object
	 */
	@PUT
	@ApiOperation(value = "Update Processor",
			notes = "revise details of valid processor",
			position = 4,
			response = com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO.class)
	@Path("/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Update  processor", required = true,
			dataType = "com.liaison.mailbox.swagger.dto.request.ReviseProcessorRequest", paramType = "body") })
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "reviseProcessor")
	public Response reviseProcessor(
			@Context HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") String guid) {

		// Audit LOG the Attempt to reviseProcessor
		auditAttempt("reviseProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		ReviseProcessorRequestDTO serviceRequest;

		try (InputStream requestStream = request.getInputStream()) {

			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, ReviseProcessorRequestDTO.class);

			ReviseProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// updates existing processor
			serviceResponse = mailbox.reviseProcessor(serviceRequest, mailboxguid, guid);

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "reviseProcessor");

			// constructs response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProcessorConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("reviseProcessor");
		}
		return returnResponse;
	}
	
	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
				PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
	}

	@Override
	protected void beginMetricsCollection() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endMetricsCollection(boolean success) {
		// TODO Auto-generated method stub

	}

}
