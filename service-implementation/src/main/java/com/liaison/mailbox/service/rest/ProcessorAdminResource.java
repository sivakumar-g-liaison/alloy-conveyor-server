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
 * This is the gateway processors state monitoring and interrupt.
 *
 * @author OFS
 */

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.stats.StatsConfig;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/processoradmin/processor/execution")
@Api(value = "config/mailbox/processoradmin", description = "Administration of processor services")
public class ProcessorAdminResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	private Stopwatch stopwatch;
	private static final StatsTimer statsTimer = new StatsTimer(
            MonitorConfig.builder("ProcessorAdminResource_statsTimer").build(),
            new StatsConfig.Builder().build());
	
	static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }
	
	public ProcessorAdminResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST service to get the list processors latest state
	 * 
	 * @param HttpServletRequest
	 * @param status
	 * @param frmDate
	 * @param toDate
	 * @param hitCounter
	 * @return Response
	 */
	@GET
	@ApiOperation(value = "Get Executing Processors", notes = "get list of executing processors", position = 21, response = com.liaison.mailbox.service.dto.ui.GetExecutingProcessorResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response getExecutingProcessors(
			@Context HttpServletRequest request,
			@QueryParam(value = "status") @ApiParam(name = "status", required = false, value = "get list of executing processors with the status specified") final String status,
			@QueryParam(value = "frmDate") @ApiParam(name = "frmDate", required = false, value = "get list of executing processors from the date specified ") final String frmDate,
			@QueryParam(value = "toDate") @ApiParam(name = "toDate", required = false, value = "get list of executing processors to the date specified") final String toDate,
			@QueryParam(value = "hitCounter") @ApiParam(name = "hitCounter", required = false, value = "hitCounter") final String hitCounter) {


		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				try {
					GetExecutingProcessorResponseDTO serviceResponse = null;
					ProcessorConfigurationService processor = new ProcessorConfigurationService();
					// get the list processors latest state
					serviceResponse = processor.getExecutingProcessors(status, frmDate, toDate);
					serviceResponse.setHitCounter(hitCounter);

					return serviceResponse;
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "ProcessorAdminResource.getExecutingProcessors()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);
		worker.queryParams.put("frmDate", frmDate);
		worker.queryParams.put("toDate", toDate);

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
	 * REST method to interrupt the execution of running processor.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@DELETE
	@ApiOperation(value = "Interrupt processors", notes = "interrupt running processor", position = 22, response = com.liaison.mailbox.service.dto.configuration.response.InterruptExecutionEventResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Interrupt running processor", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.InterruptExecutionRequest", paramType = "body") })
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response interruptRunningProcessor(
			@Context final HttpServletRequest request,
			@QueryParam(value = "executionID") @ApiParam(name = "executionID", required = false, value = "executionID") final String executionID) {


		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				try {

					ProcessorConfigurationService processor = new ProcessorConfigurationService();
					// creates new execution event
					return processor.interruptRunningProcessor(executionID);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "ProcessorAdminResource.interruptRunningProcessor()";
		worker.queryParams.put("executionID", executionID);

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
		
		stopwatch = statsTimer.start();
        int globalCount = globalServiceCallCounter.addAndGet(1);
        logKPIMetric(globalCount, "Global_serviceCallCounter");
        int serviceCount = serviceCallCounter.addAndGet(1);
        logKPIMetric(serviceCount, "ProcessorAdminResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {
		
		stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "ProcessorAdminResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "ProcessorAdminResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "ProcessorAdminResource_failureCounter");
        }
	}
}
