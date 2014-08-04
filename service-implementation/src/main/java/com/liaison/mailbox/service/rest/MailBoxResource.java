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

import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.mailbox.service.core.MailBoxService;
import com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO;
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
 * 
 * @author OFS
 *
 */
@Path("v1/mailbox/triggerProfile")
@Api(value = "v1/mailbox/triggerProfile", description = "Trigger profile services")
public class MailBoxResource extends BaseResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public MailBoxResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST method to trigger a profile and run the processors in that profile.
	 * 
	 * @param profileName
	 *            The profile name to be trigger
	 * @param mailboxNamePattern
	 *            The mailbox name pattern. The given pattern mailbox do not include in the trigger
	 *            profile process.
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Trigger profile",
	notes = "trigger a profile",
	position = 23,
	response = com.liaison.mailbox.service.dto.configuration.response.TriggerProfileResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
		@ApiResponse( code = 500, message = "Unexpected Service failure." )
	})
	@AccessDescriptor(skipFilter=true)
	public Response triggerProfile(@QueryParam(value = "name") @ApiParam(name="name", required=true, value="The name of the profile to be triggered") String profileName,
			@QueryParam(value = "excludeMailbox") @ApiParam(name="excludeMailbox", required=false, value="The name of the mailbox to be excluded. The given mailbox is not included in the trigger profile process.") String mailboxNamePattern,
			@QueryParam(value = "shardKey") @ApiParam(name="name", required=false, value="shared Key") String shardKey) {

		//Audit LOG the Attempt to triggerProfile
		auditAttempt("triggerProfile");
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			LOG.debug("Entering into trigger profile resource.");
			MailBoxService service = new MailBoxService();
			TriggerProfileResponseDTO serviceResponse = service.triggerProfile(profileName, mailboxNamePattern, shardKey);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "triggerProfile");
			
			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			// should be throwing out of domain scope and into framework using above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			//Audit LOG the failure
			auditFailure("triggerProfile");
		}
		return returnResponse;

	}

}
