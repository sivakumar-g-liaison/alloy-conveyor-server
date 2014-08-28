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
import com.liaison.mailbox.service.core.MailboxSLAService;
import com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * 
 * @author OFS
 *
 */
@Path("mailbox/sla")
@Api(value = "mailbox/sla", description = "Checks whether Mailbox Configurations satisfies the expectations as per SLA")
public class MailboxSLAResource extends AuditedResource {
	
	private static final Logger LOG = LogManager.getLogger(MailBoxFileResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	

	public MailboxSLAResource() {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	
	/**
	 * REST method to validate the sla rules of all mailboxes.
	 * 
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Check Mailbox satifies the expectations",
	notes = "Check Mailbox satifies the expectations",
	position = 23,
	response = com.liaison.mailbox.service.dto.configuration.response.MailboxSLAResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
		@ApiResponse( code = 500, message = "Unexpected Service failure." )
	})
	@AccessDescriptor(skipFilter=true)
	public Response validateMailboxSLA() {

		//Audit LOG the Attempt to triggerProfile
		auditAttempt("validateMailboxSLA");
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			LOG.debug("Entering into SLA Validation");
			MailboxSLAService service = new MailboxSLAService();
			MailboxSLAResponseDTO serviceResponse = service.validateMailboxSLARules();

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "validateMailboxSLA");
			
			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			// should be throwing out of domain scope and into framework using above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			//Audit LOG the failure
			auditFailure("validateMailboxSLA");
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
