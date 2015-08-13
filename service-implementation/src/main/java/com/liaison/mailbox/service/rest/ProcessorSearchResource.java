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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

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
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
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
@Path("config/mailbox/searchprocessor")
@Api(value = "config/mailbox/searchprocessor", description = "Administration of processor services")
public class ProcessorSearchResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(ProcessorSearchResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	private Stopwatch stopwatch;
    private static final StatsTimer statsTimer = new StatsTimer(
            MonitorConfig.builder("ProcessorSearchResource_statsTimer").build(),
            new StatsConfig.Builder().build());

    static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }

	public ProcessorSearchResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * REST service to get all the processors
	 * 
	 * @param HttpServletRequest
	 * @return Response
	 */
	@GET
	@ApiOperation(value = "Get All Processors", notes = "get all the processors", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response searchProcessor(@Context HttpServletRequest request,
			@QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
			@QueryParam(value = "pagesize") @ApiParam(name = "pagesize", required = false, value = "pagesize") final String pageSize,
			@QueryParam(value = "sortField") @ApiParam(name = "sortField", required = false, value = "sortField") final String sortField,
			@QueryParam(value = "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") final String sortDirection,
			@QueryParam(value = "mbxName") @ApiParam(name = "mbxName", required = false, value = "mbxName") final String mbxName,
			@QueryParam(value = "pipelineId") @ApiParam(name = "pipelineId", required = false, value = "pipelineId") final String pipelineId,
			@QueryParam(value = "folderPath") @ApiParam(name = "folderPath", required = false, value = "folderPath") final String folderPath,
			@QueryParam(value = "profileName") @ApiParam(name = "profileName", required = false, value = "profileName") final String profileName,
			@QueryParam(value = "protocol") @ApiParam(name = "protocol", required = false, value = "protocol") final String protocol,
			@QueryParam(value = "prcsrType") @ApiParam(name = "prcsrType", required = false, value = "prcsrType") final String prcsrType) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

				serviceCallCounter.addAndGet(1);

				try {

					ProcessorConfigurationService processor = new ProcessorConfigurationService();
					GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
					searchFilter.setPage(page);
					searchFilter.setPageSize(pageSize);
					searchFilter.setSortField(sortField);
					searchFilter.setSortDirection(sortDirection);
					searchFilter.setMbxName(mbxName);
					searchFilter.setPipelineId(pipelineId);
					searchFilter.setFolderPath(folderPath);
					searchFilter.setProfileName(profileName);
					searchFilter.setProtocol(protocol);
					searchFilter.setProcessorType(prcsrType);

					// Get all the processors
					return processor.searchProcessor(searchFilter);
				} catch (IOException | JAXBException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				} catch (SymmetricAlgorithmException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "ProcessorSearchResource.getAllProcessors()";
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
        logKPIMetric(serviceCount, "ProcessorSearchResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {

	    stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "ProcessorSearchResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "ProcessorSearchResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "ProcessorSearchResource_failureCounter");
        }

	}
}
