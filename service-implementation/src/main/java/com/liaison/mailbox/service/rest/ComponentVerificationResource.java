/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.mailbox.service.core.component.verification.ComponentVerificationService;
import com.liaison.mailbox.service.dto.configuration.response.ComponentVerificationDTO;

/**
 * Verifying following components in Mailbox : DB, FS2, ACL, Bootstrap,
 * Environment and Version
 * 
 */
@Path("/verifycomponent")
public class ComponentVerificationResource extends AuditedResource  {

	@GET
	@Produces("application/json")
	public Response verifyComponents(@Context final HttpServletRequest request) throws Exception {
		
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

				ComponentVerificationService compService = new ComponentVerificationService();
				List<ComponentVerificationDTO> responseDto = compService.verifyComponents();
				return marshalResponse(200, MediaType.APPLICATION_JSON, responseDto);
			}
		};
		worker.actionLabel = "ComponentVerificationResource.verifyComponents()";

		// handle the delegate to the framework for calling
		return process(request, worker);
	}

	/**
	 * Initial audit statement common to exports service requests.
	 * 
	 * @param actionLabel
	 * @return
	 */
	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		
		return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
				PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
	}

}