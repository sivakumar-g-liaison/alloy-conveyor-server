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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
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
@Path("config/mailbox/profile")
@Api(value = "config/mailbox/profile", description = "Gateway for the profile configuration services.")
public class MailBoxProfileResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxProfileResource.class);

	/**
	 * REST method to initiate profile creation.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Profile", notes = "create a new profile", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Create new profile", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.AddProfileRequest", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response createProfile(@Context final HttpServletRequest request) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				String requestString;
				try {
					requestString = getRequestBody(request);
					AddProfileRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							AddProfileRequestDTO.class);
					// creates new profile
					ProfileConfigurationService profile = new ProfileConfigurationService();
					AddProfileResponseDTO serviceResponse = profile.createProfile(serviceRequest); 
					//Added the guid
					if (null != serviceResponse.getProfile()) {
					    queryParams.put(AuditedResource.HEADER_GUID, serviceResponse.getProfile().getGuId());
					}
					return serviceResponse;

				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "MailBoxProfileResource.createProfile()";

		// hand the delegate to the framework for calling
		return process(request, worker);
	}

	/**
	 * REST method to update a profile
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@PUT
	@ApiOperation(value = "Update profile", notes = "Update an existing profile", position = 2, response = com.liaison.mailbox.service.dto.configuration.response.ReviseProfileResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Update an existing profile", required = true, dataType = "com.liaison.mailbox.swagger.dto.request.ReviseProfileRequest", paramType = "body")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response reviseProfile(@Context final HttpServletRequest request) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				String requestString;
				try {
					requestString = getRequestBody(request);
					ReviseProfileRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							ReviseProfileRequestDTO.class);
					//Added the guid
					if (null != serviceRequest.getProfile()) {
					    queryParams.put(AuditedResource.HEADER_GUID, serviceRequest.getProfile().getId());
					}
					// update profile
					ProfileConfigurationService profile = new ProfileConfigurationService();
					return profile.updateProfile(serviceRequest);
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "MailBoxProfileResource.updateProfile()";

		// hand the delegate to the framework for calling
		return process(request, worker);
	}

	/**
	 * REST method to retrieve all profiles.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "List Profiles", notes = "returns detail information of all the profiles", position = 3, response = com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response readProfiles(
			@Context final HttpServletRequest request,
			@ApiParam(value = "Page Number", required = false) @QueryParam(value = "page") final String page,
			@ApiParam(value = "Page Size", required = false) @QueryParam(value = "pageSize") final String pageSize,
			@ApiParam(value = "Sorting Information", required = false) @QueryParam(value = "sortInfo") final String sortInfo,
			@ApiParam(value = "Filter Text", required = false) @QueryParam(value = "filterText") final String filterText) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				// read Profiles
				ProfileConfigurationService profile = new ProfileConfigurationService();
				return profile.getProfiles(page, pageSize, sortInfo, filterText);
			}
		};
		worker.actionLabel = "MailBoxProfileResource.readProfiles()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);
		worker.queryParams.put("filterText", filterText);

		// hand the delegate to the framework for calling
		return process(request, worker);
	}

	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
				PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
	}

}
