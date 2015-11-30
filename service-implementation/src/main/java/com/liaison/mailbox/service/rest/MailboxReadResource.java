package com.liaison.mailbox.service.rest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
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
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
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
 * This is the gateway for the profile configuration services.
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("config/entity/read/{type}/{guid}")
@Api(value = "config/entity/read/{type}/{guid}", description = "Gateway for the entity read service.")
public class MailboxReadResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailboxReadResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	private Stopwatch stopwatch;
	private static final StatsTimer statsTimer = new StatsTimer(
            MonitorConfig.builder("MailboxReadResource_statsTimer").build(),
            new StatsConfig.Builder().build());
	
	static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }
	
	public MailboxReadResource() throws IOException {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	@GET
	@ApiOperation(value = "read an entity", notes = "returns details of an entity", position = 3, response = com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response readEntity(
			@Context final HttpServletRequest request,
			@PathParam(value = "type") final @ApiParam(name = "type", required = true, value = "type") String type,
			@PathParam(value = "guid") final @ApiParam(name = "guid", required = true, value = "guid") String guid) {

		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

			@Override
			public Object call() throws Exception {

				serviceCallCounter.incrementAndGet();
				LOG.info("The requested type is {}", type);
				switch (type.toLowerCase()) {
				
					case MailBoxConstants.TYPE_PROFILE :
						 return new ProfileConfigurationService().getProfileByGuid(guid);
					case MailBoxConstants.TYPE_PROCESSOR :
						 return new ProcessorConfigurationService().getProcessor(guid);
					case MailBoxConstants.TYPE_MAILBOX :
						 return new MailBoxConfigurationService().readMailbox(guid);
					default:
						 return marshalResponse(Response.Status.BAD_REQUEST.getStatusCode(), MediaType.TEXT_PLAIN, "Unsupported entity type");
				}
			}
		};
		worker.actionLabel = "MailboxReadResource.readEntity()";
		worker.queryParams.put("type", type);
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
		return new DefaultAuditStatement(AuditStatement.Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
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
        logKPIMetric(serviceCount, "MailboxReadResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {

		stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "MailboxReadResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "MailboxReadResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "MailboxReadResource_failureCounter");
        }
	}

}
