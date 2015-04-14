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
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
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
 * @author veerasamyn
 */
@AppConfigurationResource
@Path("config/mailbox")
@Api(value = "config/mailbox", description = "Gateway for the mailbox configuration services.")
public class MailBoxConfigurationResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public MailBoxConfigurationResource()
			throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST method to initiate mailbox creation.
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Mailbox", notes = "create a new mailbox", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Create new mailbox", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.AddMailBoxRequest", paramType = "body") })
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response createMailBox(
			@Context final HttpServletRequest request,
			@QueryParam(value = "sid") @ApiParam(name = "sid", required = true, value = "Service instance id") final String serviceInstanceId) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				String requestString;
				try {
					requestString = getRequestBody(request);
					AddMailboxRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							AddMailboxRequestDTO.class);

					// retrieving acl manifest from header
					LOG.info("Retrieving acl manifest json from request header");
					String manifestJson = MailBoxUtil.getManifest(request.getHeader("acl-manifest"));
					// creates new mailbox
					MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
					return mailbox.createMailBox(serviceRequest, serviceInstanceId, manifestJson);
				} catch (IOException | JAXBException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "MailBoxConfigurationResource.createMailBox()";

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
	 * Rest method to search the mailbox based on the given query parameters. If both are empty it returns all
	 * mailboxes.
	 *
	 * @param mbxName The mailbox name should be searched
	 * @param profileName The profile name should be searched
	 * @return The Response
	 */
	@GET
	@ApiOperation(value = "Search Mailbox", notes = "search a mailbox using given query parameters", position = 1, response = com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response searchMailBox(
			@Context final HttpServletRequest request,
			@QueryParam(value = "name") @ApiParam(name = "name", required = false, value = "Name of the mailbox to be searched. Either mailbox name or profile name is mandatory.") final String mbxName,
			@QueryParam(value = "profile") @ApiParam(name = "profile", required = false, value = "Name of the profile to be searched. Either mailbox name or profile name is mandatory.") final String profileName,
			@QueryParam(value = "hitCounter") @ApiParam(name = "hitCounter", required = false, value = "hitCounter") final String hitCounter,
			@QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
			@QueryParam(value = "pagesize") @ApiParam(name = "pagesize", required = false, value = "pagesize") final String pageSize,
			@QueryParam(value = "sortField") @ApiParam(name = "sortField", required = false, value = "sortField") final String sortField,
			@QueryParam(value = "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") final String sortDirection,
			@QueryParam(value = "siid") @ApiParam(name = "siid", required = true, value = "service instance id") final String serviceInstanceId) {


		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				try {
					// search the mailbox from the given details
					MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
					// retrieving acl manifest from header
					LOG.info("Retrieving acl manifest json from request header");
					String manifestJson = MailBoxUtil.getManifest(request.getHeader("acl-manifest"));

					GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
					searchFilter.setMbxName(mbxName);
					searchFilter.setServiceInstanceId(serviceInstanceId);
					searchFilter.setProfileName(profileName);
					searchFilter.setPage(page);
					searchFilter.setPageSize(pageSize);
					searchFilter.setSortField(sortField);
					searchFilter.setSortDirection(sortDirection);
					// search the mailbox based on the given query parameters
					SearchMailBoxResponseDTO serviceResponse = mailbox.searchMailBox(searchFilter, manifestJson);
					serviceResponse.setHitCounter(hitCounter);

					return serviceResponse;
				} catch (IOException | JAXBException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "MailboxConfigurationResource.searchMailBox()";
		worker.queryParams.put("name", mbxName);
		worker.queryParams.put("profile", profileName);

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