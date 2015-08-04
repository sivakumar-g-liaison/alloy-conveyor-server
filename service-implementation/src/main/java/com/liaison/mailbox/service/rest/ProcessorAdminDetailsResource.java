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

/**
 * This is the gateway updating and retrieving processor state.
 *
 * @author OFS
 */

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAO;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorExecutionStateResponseDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/processoradmin/processor/status")
@Api(value = "config/mailbox/processoradmin/processor/changeStatus", description = "Change the processor status to failed")
public class ProcessorAdminDetailsResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public ProcessorAdminDetailsResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST service to get the list processors latest state
	 * 
	 * @param HttpServletRequest
	 * @return Response
	 */
	@PUT
	@ApiOperation(value = "Update Processor Status to Failed", notes = "update existing processor status to failed", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response updateProcessorStatusToFailed(
			@Context final HttpServletRequest request,
			@QueryParam(value = "processorId") final @ApiParam(name = "processorId", required = true, value = "Processor id") String processorId) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);
				ProcessorExecutionStateDAO processorDao = new ProcessorExecutionStateDAOBase();
				GetProcessorExecutionStateResponseDTO serviceResponse = new GetProcessorExecutionStateResponseDTO();

				ProcessorExecutionState processorExecutionState = processorDao.findByProcessorId(processorId);
				if(null != processorExecutionState) {
					processorDao.addProcessorExecutionState(processorId, ExecutionState.FAILED.value());
					serviceResponse.setResponse(new ResponseDTO(Messages.REVISED_SUCCESSFULLY, "The processor execution status for processor with id : " + processorId + " is ", Messages.SUCCESS));
				}
				return serviceResponse;
			}
		};
		worker.actionLabel = "ProcessorAdminDetailsResource.updateProcessorStatusToFailed()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, processorId);

		// hand the delegate to the framework for calling
		try {
			return handleAuditedServiceRequest(request, worker);
		} catch (LiaisonAuditableRuntimeException e) {
			if (!StringUtils.isEmpty(e.getResponseStatus().getStatusCode() + "")) {
				return marshalResponse(e.getResponseStatus().getStatusCode(), MediaType.TEXT_PLAIN, e.getMessage());
			}
			return marshalResponse(500, MediaType.TEXT_PLAIN, e.getMessage());
		}
	}

	/**
	 * REST service to get the list processors that are in executing state
	 * @return Response
	 */
	@GET
	@ApiOperation(value = "Get Executing Processors", notes = "get list of executing processors", position = 21, response = com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response getExecutingProcessors(
			@Context HttpServletRequest request) {


		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);
				ProcessorExecutionStateDAO processorDao = new ProcessorExecutionStateDAOBase();
				GetProcessorExecutionStateResponseDTO serviceResponse = new GetProcessorExecutionStateResponseDTO();

				serviceResponse.setExecutingProcessorIds(processorDao.findExecutingProcessors());
				serviceResponse.setResponse(new ResponseDTO(Messages.READ_SUCCESSFUL, "The list of executing processors are " , Messages.SUCCESS));
				return serviceResponse;
			}
		};
		worker.actionLabel = "ProcessorAdminDetailsResource.getExecutingProcessors()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);

		// hand the delegate to the framework for calling
		try {
			return handleAuditedServiceRequest(request, worker);
		} catch (LiaisonAuditableRuntimeException e) {
			if (!StringUtils.isEmpty(e.getResponseStatus().getStatusCode() + "")) {
				return marshalResponse(e.getResponseStatus().getStatusCode(), MediaType.TEXT_PLAIN, e.getMessage());
			}
			return marshalResponse(500, MediaType.TEXT_PLAIN, e.getMessage());
		}
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
