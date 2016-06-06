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
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
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
 * This is the gateway for the mailbox configuration services.
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("config/mailbox/{id}")
@Api(value = "config/mailbox/{id}", description = "Gateway for the mailbox configuration services.")
public class MailboxConfigurationDetailsResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailboxConfigurationDetailsResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	private Stopwatch stopwatch;
	private static final StatsTimer statsTimer = new StatsTimer(
            MonitorConfig.builder("MailboxConfigurationDetailsResource_statsTimer").build(),
            new StatsConfig.Builder().build());
	
	static {
        DefaultMonitorRegistry.getInstance().register(statsTimer);
    }
	
	public MailboxConfigurationDetailsResource()
			throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST method to update existing mailbox.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@PUT
	@ApiOperation(value = "Update Mailbox", notes = "update details of existing mailbox", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO.class)
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Update existing mailbox", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.ReviseMailBoxRequest", paramType = "body") })
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response reviseMailBox(
			@Context final HttpServletRequest request,
			@PathParam(value = "id") final @ApiParam(name = "id", required = true, value = "mailbox guid") String guid,
			@QueryParam(value = "sid") final @ApiParam(name = "sid", required = true, value = "Service instance id") String serviceInstanceId,
			@QueryParam(value = "addServiceInstanceIdConstraint") final @ApiParam(name = "addServiceInstanceIdConstraint", required = true, value = "Service instance id constraint") boolean addConstraint) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				String requestString;
				try {
					requestString = getRequestBody(request);
					ReviseMailBoxRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							ReviseMailBoxRequestDTO.class);

					// retrieving acl manifest from header
					LOG.debug("Retrieving acl manifest json from request header");
					String manifestJson = request.getHeader("acl-manifest");
					// updates existing mailbox
					MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
					return mailbox.reviseMailBox(serviceRequest, guid, serviceInstanceId, manifestJson, addConstraint);
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailboxConfigurationDetailsResource.reviseMailBox()";
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
	 * REST method to delete a mailbox.
	 * 
	 * @param guid The id of the mailbox
	 * 
	 * @return Response Object
	 */
	@DELETE
	@ApiOperation(value = "Delete Mailbox", notes = "delete a mailbox", position = 2, response = com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response deactivateMailBox(@Context final HttpServletRequest request,
			@PathParam(value = "id") @ApiParam(name = "id", required = true, value = "mailbox guid") final String guid) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				try {
					// retrieving acl manifest from header
					LOG.debug("Retrieving acl manifest json from request header");
					String manifestJson = request.getHeader("acl-manifest");
					// deactivates existing mailbox
					MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
					return mailbox.deactivateMailBox(guid, manifestJson);
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailboxConfigurationDetailsResource.deactivateMailBox()";
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
	 * @param guid The id of the mailbox
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Mailbox Details", notes = "returns details of a valid mailbox", position = 3, response = com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response readMailBox(
			@Context final HttpServletRequest request,
			@PathParam(value = "id") final @ApiParam(name = "id", required = true, value = "mailbox guid") String guid,
			@QueryParam(value = "addServiceInstanceIdConstraint") final @ApiParam(name = "addServiceInstanceIdConstraint", required = true, value = "Service instance id constraint") boolean addConstraint,
			@QueryParam(value = "sid") final @ApiParam(name = "sid", required = true, value = "Service instance id") String serviceInstanceId) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				try {
					// retrieving acl manifest from header
					LOG.debug("Retrieving acl manifest json from request header");
					String manifestJson = request.getHeader("acl-manifest");
					// deactivates existing mailbox
					MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
					return mailbox.getMailBox(guid, addConstraint, serviceInstanceId, manifestJson);
				} catch (IOException | JAXBException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				} catch (SymmetricAlgorithmException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to read a mailbox. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailboxConfigurationDetailsResource.readMailBox()";
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
        logKPIMetric(serviceCount, "MailboxConfigurationDetailsResource_serviceCallCounter");
	}

	@Override
	protected void endMetricsCollection(boolean success) {
		
		stopwatch.stop();
        long duration = stopwatch.getDuration(TimeUnit.MILLISECONDS);
        globalStatsTimer.record(duration, TimeUnit.MILLISECONDS);
        statsTimer.record(duration, TimeUnit.MILLISECONDS);

        logKPIMetric(globalStatsTimer.getTotalTime() + " elapsed ms/" + globalStatsTimer.getCount() + " hits",
                "Global_timer");
        logKPIMetric(statsTimer.getTotalTime() + " ms/" + statsTimer.getCount() + " hits", "MailboxConfigurationDetailsResource_timer");
        logKPIMetric(duration + " ms for hit " + statsTimer.getCount(), "MailboxConfigurationDetailsResource_timer");

        if (!success) {
            logKPIMetric(globalFailureCounter.addAndGet(1), "Global_failureCounter");
            logKPIMetric(failureCounter.addAndGet(1), "MailboxConfigurationDetailsResource_failureCounter");
        }
	}

}
