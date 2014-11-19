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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import com.liaison.mailbox.service.core.sla.MailboxSLAWatchDogService;
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
@Path("mailbox/trigger/slacheck")
@Api(value = "mailbox/trigger/slacheck", description = "Checks whether Mailbox Configurations satisfies the expectations as per SLA")
public class TriggerSLAEvaluvationResource extends AuditedResource {
	
	private static final Logger LOG = LogManager.getLogger(TriggerSLAEvaluvationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	

	public TriggerSLAEvaluvationResource() {

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
	public Response validateMailboxSLA(@Context final HttpServletRequest request) {
        
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {
				
				serviceCallCounter.addAndGet(1);				
				try {
					LOG.debug("Entering into Mailbox SLA Validation");
					//validate the sla rules of all mailboxes
					MailboxSLAWatchDogService service = new MailboxSLAWatchDogService();
					
					return service.validateSLARules();					
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					e.printStackTrace();
					throw new LiaisonRuntimeException("Failed to validate SLA." + e.getMessage());
					
				}			
				
			}
		};
		worker.actionLabel = "TriggerSLAEvaluvationResource.validateMailboxSLA()";

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
