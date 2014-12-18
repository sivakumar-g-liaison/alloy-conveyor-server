/**
 * Copyright 2014 Liaison Technologies, Inc.
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
import javax.ws.rs.Consumes;
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

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.mailbox.service.core.ScriptService;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
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
 * This is the gateway for the script file details services.
 *
 * @author OFS
 */

@Path("mailbox/git/content/{git.file.name}")
@Api(value = "mailbox/git/content/{git.file.name}", 
description = "Gateway for the script file details services")
public class MailBoxScriptDetailsConfigurationResource extends AuditedResource {	

	private static final Logger LOG = LogManager.getLogger(MailBoxScriptDetailsConfigurationResource.class);
	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);	
	
	private Stopwatch stopwatch;
	private static final StatsTimer statsTimer = new StatsTimer(MonitorConfig.builder("MailBoxScriptDetailsConfigurationResource_statsTimer")
			.build(), new StatsConfig.Builder().build());

	static {
		DefaultMonitorRegistry.getInstance().register(statsTimer);
	}
	
	/**
	 *
	 * Rest method to fetch script file from git
	 *
	 * @param request
	 * @param pguid
	 * @return Response
	 */
	@GET
	@ApiOperation(value = "fetch the script file by url",
   notes = "The script file are loaded from GitLab repository",
   position = 3,
   response=com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "fetch script file", required = true,
	dataType = "com.liaison.mailbox.service.dto.configuration.response.ScriptServiceResponseDTO", paramType = "body") })
   @ApiResponses({
	@ApiResponse( code = 500, message = "Unexpected Service failure." )
	})
   
	public Response readScript(
			@Context final HttpServletRequest request,			
			@PathParam(value = "git.file.name") @ApiParam(name = "git.file.name", required = true, value = "URL where file is going to be fetch") final String gitFileName) throws Exception {
      
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {
				
				serviceCallCounter.addAndGet(1);
				try {
					
					ScriptService scriptService = new ScriptService();
					return scriptService.getScript(gitFileName, "Unknow user");
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				}       
				
			}
		};
		worker.actionLabel = "MailBoxScriptDetailsConfigurationResource.readScript()";

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
		logKPIMetric(serviceCount, "IdpProfileResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {

		stopwatch.stop();
		long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
		globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
		statsTimer.record(duration, TimeUnit.MILLISECONDS);

		logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
				"Global_timer");
		logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "IdpProfileResource_timer");
		logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "IdpProfileResource_timer");

		if (!success) {
			logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
			logKPIMetric(failureCounter.addAndGet(1), "IdpProfileResource_failureCounter");
		}
	}
}
