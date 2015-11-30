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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
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

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	private Stopwatch stopwatch;
	private static final StatsTimer statsTimer = new StatsTimer(
            MonitorConfig.builder("MailBoxProcessorDetailsResource_statsTimer").build(),
            new StatsConfig.Builder().build());
	
	static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }
	
	public MailBoxProcessorDetailsResource()
			throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}


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
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response deleteProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") final String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") final String guid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				// Deactivating processor
				ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
				return mailbox.deactivateProcessor(mailboxguid, guid);
			}
		};
		worker.actionLabel = "MailBoxProcessorDetailsResource.deleteProcessor()";
		worker.queryParams.put("mailboxid", mailboxguid);
		worker.queryParams.put(AuditedResource.HEADER_GUID, guid);

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
	 * REST method to retrieve a mailbox details.
	 * 
	 * @param mailboxguid The id of the mailbox
	 * @param guid The id of the processor
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Processor Details", notes = "returns detail information of a valid processor", position = 3, response = com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response getProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") final String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") final String guid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call()
					throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

				serviceCallCounter.addAndGet(1);
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
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Update  processor", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.ReviseProcessorRequest", paramType = "body") })
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response reviseProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name = "mailboxid", required = true, value = "mailbox guid") final String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name = "processorid", required = true, value = "processor id") final String guid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call()
					throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

				serviceCallCounter.addAndGet(1);
				String requestString;
				try {
					requestString = getRequestBody(request);
					ReviseProcessorRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							ReviseProcessorRequestDTO.class);
					// updates existing processor
					ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
					return mailbox.reviseProcessor(serviceRequest, mailboxguid, guid);

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
        logKPIMetric(serviceCount, "MailBoxProcessorDetailsResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {
		
		stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "MailBoxProcessorDetailsResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "MailBoxProcessorDetailsResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "MailBoxProcessorDetailsResource_failureCounter");
        }
	}

}
