/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.exception;

/**
 * This is the gateway for the mailbox configuration services.
 * 
 * @author santoshc
 */

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.util.StreamUtil;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddFSMExecutionEventRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddFSMExecutionEventResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO;
import com.liaison.mailbox.service.rest.BaseResource;
import com.liaison.mailbox.service.rest.MailBoxConfigurationResource;
import com.liaison.mailbox.service.util.MailBoxUtility;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("v1/mailbox/processoradmin")
@Api(value = "v1/mailbox/processoradmin", description = "Administration of processor services")
public class MailboxAdminResource extends BaseResource{
	
	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	public MailboxAdminResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * REST service to get the list processors latest state
	 * @param HttpServletRequest
	 * @param status
	 * @param frmDate
	 * @param toDate
	 * @param hitCounter
	 * @return Response
	 */
	@GET
	@ApiOperation(value = "Get Executing Processor", notes = "executing processor", position = 21)
	@Path("/processor/execution")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExecutingProcessors(@Context HttpServletRequest request, @QueryParam(value = "status") String status,
			@QueryParam(value = "frmDate") String frmDate, @QueryParam(value = "toDate") String toDate, @QueryParam(value = "hitCounter") String hitCounter) {

		// Audit LOG the Attempt to getExecutingProcessors
		auditAttempt("getExecutingProcessors");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {
			GetExecutingProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService processor = new ProcessorConfigurationService();

			serviceResponse = processor.getExecutingProcessors(status, frmDate, toDate);
			serviceResponse.setHitCounter(hitCounter);

			// Audit LOG
			if (serviceResponse.getResponse().getStatus() == "success") {
				auditSuccess("getExecutingProcessors");
			} else {
				auditFailure("getExecutingProcessors");
			}

			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			// Audit LOG the failure
			auditFailure("getExecutingProcessors");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		return returnResponse;
	}

	/**
	 * REST method to initiate FSM execution event creation.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Event", notes = "interrupt running processor", position = 22)
	@Path("/processor/executionEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response interruptRunningProcessor(@Context HttpServletRequest request) {

		// Audit LOG the Attempt to interruptRunningProcessor
		auditAttempt("interruptRunningProcessor ");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;
		AddFSMExecutionEventRequestDTO serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtility.unmarshalFromJSON(requestString, AddFSMExecutionEventRequestDTO.class);

			AddFSMExecutionEventResponseDTO serviceResponse = null;
			ProcessorConfigurationService processor = new ProcessorConfigurationService();

			// creates new execution event
			serviceResponse = processor.interruptRunningProcessor(serviceRequest);

			// Audit LOG
			if (serviceResponse.getResponse().getStatus() == "success") {
				auditSuccess("interruptRunningProcessor");
			} else {
				auditFailure("interruptRunningProcessor");
			}

			// populate the response body
			return serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("interruptRunningProcessor");
		return returnResponse;

	}
}
