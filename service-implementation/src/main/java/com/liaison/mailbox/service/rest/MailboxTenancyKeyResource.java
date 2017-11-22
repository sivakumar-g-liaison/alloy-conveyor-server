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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
import com.liaison.mailbox.service.core.MailboxTenancyKeyService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway to retrieve all tenancy keys of acl manifest in request.
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("config/mailbox/tenancyKeys")
@Api(value = "config/mailbox/tenancyKeys", description = "gateway to retrieve all tenancy keys of acl manifest in request")
public class MailboxTenancyKeyResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailboxTenancyKeyResource.class);

	/**
	 * REST method to retrieve all domains from acl-manifest present in request header.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Tenancy Keys present in acl-manifest in header", notes = "returns tenancy keys present in acl-manifest header", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.GetTenancyKeysResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response retrieveTenancyKeys(@Context final HttpServletRequest request) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {
				try {
					
					// retrieving acl manifest from header
					LOG.debug("Retrieving acl manifest json from request header");
					String manifestJson = request.getHeader("acl-manifest");
					// retrieve TenancyKeys
					MailboxTenancyKeyService mailboxTenancyKey = new MailboxTenancyKeyService();
					return mailboxTenancyKey.getAllTenancyKeysFromACLManifest(manifestJson);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}

			}
		};
		worker.actionLabel = "MailboxTenancyKeyResource.retrieveTenancyKeys()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);

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
