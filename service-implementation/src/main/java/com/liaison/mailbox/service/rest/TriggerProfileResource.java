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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
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
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.MailBoxService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for trigger profile services.
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("config/mailbox/trigger/profile")
@Api(value = "config/mailbox/trigger/profile", description = "Trigger profile services")
public class TriggerProfileResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(TriggerProfileResource.class);

	/**
	 * REST method to trigger a profile and run the processors in that profile.
	 * 
	 * @param profileName The profile name to be trigger
	 * @param mailboxNamePattern The mailbox name pattern. The given pattern mailbox do not include in the trigger
	 *            profile process.
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Trigger profile", notes = "trigger a profile", position = 23, response = com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	@AccessDescriptor(skipFilter = true)
	public Response triggerProfile(
			@Context final HttpServletRequest request,
			@QueryParam(value = "name") @ApiParam(name = "name", required = true, value = "The name of the profile to be triggered") final String profileName,
			@QueryParam(value = "excludeMailbox") @ApiParam(name = "excludeMailbox", required = false, value = "The name of the mailbox to be excluded. The given mailbox is not included in the trigger profile process.") final String mailboxNamePattern,
			@QueryParam(value = "shardKey") @ApiParam(name = "name", required = false, value = "shared Key") final String shardKey) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				LOG.debug("Entering into trigger profile resource.");
				MailBoxService service = new MailBoxService();
				// Trigger profile
				return service.triggerProfile(profileName, mailboxNamePattern, shardKey);
			}
		};
		worker.actionLabel = "TriggerProfileResource.triggerProfile()";
		worker.queryParams.put("name", profileName);
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
