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
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
import com.liaison.commons.util.StreamUtil;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProfileResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the profile configuration services.
 * 
 * @author OFS
 */
@Path("mailbox/profile")
@Api(value = "mailbox/profile", description = "Gateway for the profile configuration services.")
public class ProfileConfigurationResource extends AuditedResource {
	
	private static final Logger LOG = LogManager.getLogger(ProfileConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	

	public ProfileConfigurationResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * REST method to initiate profile creation.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Profile",
			notes = "create a new profile",
			position = 1,
			response = com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Create new profile", required = true,
			dataType = "com.liaison.mailbox.swagger.dto.request.AddProfileRequest", paramType = "body") })
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "createProfile")
	public Response createProfile(@Context HttpServletRequest request) {

		// Audit LOG the Attempt to createProfile
		auditAttempt("createProfile");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		AddProfileRequestDTO serviceRequest;

		try (InputStream requestStream = request.getInputStream()) {

			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, AddProfileRequestDTO.class);

			AddProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService profile = new ProfileConfigurationService();

			// creates new profile
			serviceResponse = profile.createProfile(serviceRequest);

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "createProfile");

			// populate the response body
			return serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("createProfile");
		return returnResponse;

	}
	
	/**
	 * REST method to update a profile
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@PUT
	@ApiOperation(value = "Update profile",
			notes = "Update an existing profile",
			position = 2,
			response = com.liaison.mailbox.service.dto.configuration.response.ReviseProfileResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Update an existing profile", required = true,
			dataType = "com.liaison.mailbox.swagger.dto.request.ReviseProfileRequest", paramType = "body") })
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "reviseProfile")
	public Response reviseProfile(@Context HttpServletRequest request) {

		// Audit LOG the Attempt to createProfile
		auditAttempt("reviseProfile");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		ReviseProfileRequestDTO serviceRequest;

		try (InputStream requestStream = request.getInputStream()) {

			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, ReviseProfileRequestDTO.class);

			ReviseProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService profile = new ProfileConfigurationService();

			// creates new profile
			serviceResponse = profile.updateProfile(serviceRequest);

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "reviseProfile");

			// populate the response body
			return serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("reviseProfile");
		return returnResponse;

	}
	
	/**
	 * REST method to retrieve all profiles.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "List Profiles",
			notes = "returns detail information of all the profiles",
			position = 3,
			response = com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "readProfiles")
	public Response readProfiles() {

		// Audit LOG the Attempt to readProfiles
		auditAttempt("readProfiles");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		try {

			// add the new profile details
			GetProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService mailbox = new ProfileConfigurationService();
			serviceResponse = mailbox.getProfiles();

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "readProfiles");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("readProfiles");
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
