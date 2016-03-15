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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.sla.MailboxWatchDogService;
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
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway to check and update the customer picked up file status in LENS
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("config/mailbox/trigger/status")
@Api(value = "config/mailbox/trigger/status", description = "Updates the customer picked up files status in LENS")
public class MailboxWatchDogResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailboxWatchDogResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	private Stopwatch stopwatch;
	private static final StatsTimer statsTimer = new StatsTimer(
            MonitorConfig.builder("MailboxWatchDogResource_statsTimer").build(),
            new StatsConfig.Builder().build());
	
	static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }
	
	public MailboxWatchDogResource() {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}


	/**
	 * REST method to validate the sla rules of all mailboxes.
	 * 
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Update the customer picked up file status in LENS",
		notes = "Update the customer picked up file status in LENS",
		position = 23)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	@AccessDescriptor(skipFilter = true)
	public Response updateStatus(@Context final HttpServletRequest request, 
			@QueryParam(value = "type") @ApiParam(name = "type", required = false, value = "Type of the SLA to be checked.") final String type,
			@QueryParam(value = "mailboxstatus") @ApiParam(name = "mailboxstatus", required = false, value = "status of mailbox") final String mailboxStatus) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);
				try {
					LOG.debug("Entering into mailbox watchdog resource");

					if (!MailBoxUtil.isEmpty(type) && "sweeper".equals(type.toLowerCase())) {
						// validate the sla rules of all mailboxes
						new MailboxWatchDogService().validateMailboxSLARule(mailboxStatus);
					} else {
						// To validate Mailbox sla for all mailboxes
						new MailboxWatchDogService().pollAndUpdateStatus();
					}
					return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Failed to validate SLA." + e.getMessage());

				}

			}
		};
		worker.actionLabel = "MailboxWatchDogResource.updateStatus()";

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
        logKPIMetric(serviceCount, "MailboxWatchDogResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {
		
		stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "MailboxWatchDogResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "MailboxWatchDogResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "MailboxWatchDogResource_failureCounter");
        }
	}

}
