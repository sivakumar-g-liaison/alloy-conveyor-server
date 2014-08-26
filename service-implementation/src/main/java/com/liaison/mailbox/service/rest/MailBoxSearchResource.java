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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox configuration helper services.
 * 
 * @author OFS
 */
@Path("mailbox/search")
@Api(value = "mailbox/search", 
description = "Gateway for the mailbox configuration helper services.")
public class MailBoxSearchResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxSearchResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	

	public MailBoxSearchResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * Rest method to search the mailbox based on the given query parameters. If both are empty it returns all
	 * mailboxes.
	 * 
	 * @param mbxName
	 *            The mailbox name should be searched
	 * @param profileName
	 *            The profile name should be searched
	 * @return The Response
	 */
	@GET
	@ApiOperation(value = "Search Mailbox",
			notes = "search a mailbox using given query parameters",
			position = 1,
			response = com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "searchMailBox")
	public Response searchMailBox(
			@Context HttpServletRequest request,
			@QueryParam(value = "name") @ApiParam(name = "name", required = false, value = "Name of the mailbox to be searched. Either mailbox name or profile name is mandatory.") String mbxName,
			@QueryParam(value = "profile") @ApiParam(name = "profile", required = false, value = "Name of the profile to be searched. Either mailbox name or profile name is mandatory.") String profileName,
			@QueryParam(value = "hitCounter") @ApiParam(name = "hitCounter", required = false, value = "hitCounter") String hitCounter,
			@QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") String page,
			@QueryParam(value= "pagesize") @ApiParam(name = "pagesize", required = false, value = "pagesize") String pageSize,
			@QueryParam(value= "sortField") @ApiParam(name = "sortField", required = false, value = "sortField") String sortField,
			@QueryParam(value= "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") String sortDirection) {

		// Audit LOG the Attempt to searchMailBox
		auditAttempt("searchMailBox");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			// search the mailbox from the given details
			SearchMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			// retrieving acl manifest from header
			LOG.info("Retrieving acl manifest json from request header");
			String manifestJson = request.getHeader("acl-manifest");
			String decodedManifestJson = MailBoxUtil.getDecodedManifestJson(manifestJson);

			serviceResponse = mailbox.searchMailBox(mbxName, profileName, decodedManifestJson, page, pageSize, sortField, sortDirection);
			serviceResponse.setHitCounter(hitCounter);

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "searchMailBox");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxSearchResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("searchMailBox");
		}
		return returnResponse;

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
