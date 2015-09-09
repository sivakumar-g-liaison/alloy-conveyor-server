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
 * Service to update and retrieve the processor states
 *
 * @author OFS
 */

import java.util.concurrent.TimeUnit;
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
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorExecutionStateResponseDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.stats.StatsConfig;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/processoradmin/processor/status")
@Api(value = "config/mailbox/processoradmin/processor/status", description = "Change the processor status to failed")
public class ProcessorAdminDetailsResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(ProcessorAdminDetailsResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	private Stopwatch stopwatch;
	private static final StatsTimer statsTimer = new StatsTimer(
	        MonitorConfig.builder("ProcessorAdminDetailsResource_statsTimer").build(),
	        new StatsConfig.Builder().build());

	public ProcessorAdminDetailsResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

    static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }

	/**
	 * REST service to get the list processors latest state
	 * 
	 * @param HttpServletRequest
	 * @return Response
	 */
	@PUT
	@ApiOperation(value = "Update Processor Status to Failed", notes = "update existing processor status to failed",
	    position = 1, response = com.liaison.mailbox.service.dto.configuration.response.UpdateProcessorExecutionStateResponseDTO.class)
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
				ProcessorExecutionConfigurationService configService = new ProcessorExecutionConfigurationService();
				LOG.info("Updates the processors status to failed {}", processorId);
				return configService.UpdateExecutingProcessor(processorId);
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
	public Response getExecutingProcessors(@Context HttpServletRequest request, 
			@QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
			@QueryParam(value = "pagesize") @ApiParam(name = "pagesize", required = false, value = "pagesize") final String pageSize) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);
				ProcessorExecutionConfigurationService configService = new ProcessorExecutionConfigurationService();
				GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
			 	//setting the current page and page size
				searchFilter.setPage(page);
				searchFilter.setPageSize(pageSize);
				GetProcessorExecutionStateResponseDTO serviceResponse = configService.findExecutingProcessors(searchFilter);
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

	    stopwatch = statsTimer.start();
        int globalCount = globalServiceCallCounter.addAndGet(1);
        logKPIMetric(globalCount, "Global_serviceCallCounter");
        int serviceCount = serviceCallCounter.addAndGet(1);
        logKPIMetric(serviceCount, "ProcessorAdminDetailsResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {

	    stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "ProcessorAdminDetailsResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "ProcessorAdminDetailsResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "ProcessorAdminDetailsResource_failureCounter");
        }
	}
}
