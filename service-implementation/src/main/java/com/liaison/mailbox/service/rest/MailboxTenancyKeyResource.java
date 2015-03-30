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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.MailboxTenancyKeyService;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
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
@Path("mailbox/tenancyKeys/")
@Api(value = "mailbox/tenancyKeys", description = "gateway to retrieve all tenancy keys of acl manifest in request")
public class MailboxTenancyKeyResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailboxTenancyKeyResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	public MailboxTenancyKeyResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * REST method to retrieve all domains from acl-manifest present in request header.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Tenancy Keys present in acl-manifest in header",
	notes = "returns tenancy keys present in acl-manifest header",
	position = 1,
	response = com.liaison.mailbox.service.dto.configuration.response.GetTenancyKeysResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
		@ApiResponse( code = 500, message = "Unexpected Service failure." )
	})
	
	public Response retrieveTenancyKeys(@Context final HttpServletRequest request) {
		
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {
				
				serviceCallCounter.addAndGet(1);				
				// retrieving acl manifest from header
                LOG.info("Retrieving acl manifest json from request header");
                String manifestJson = request.getHeader("acl-manifest");
                if (MailBoxUtil.isEmpty(manifestJson)) {
                    LOG.error("ACL Manifest not available in the request header");
                    throw new MailBoxConfigurationServicesException(Messages.ACL_MANIFEST_NOT_AVAILABLE,  Response.Status.BAD_REQUEST);
                } else {
                	LOG.info("ACL Manifest available in the request header");
                }
                //retrieve TenancyKeys
                MailboxTenancyKeyService mailboxTenancyKey = new MailboxTenancyKeyService();
                return mailboxTenancyKey.getAllTenancyKeysFromACLManifest(manifestJson);				
				
			}
		};
		worker.actionLabel = "MailboxTenancyKeyResource.retrieveTenancyKeys()";

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
