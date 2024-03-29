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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/typeAhead/getEntityByNames")
@Api(value = "config/mailbox/typeAhead", description = "Administration of processor services")
public class TypeaheadResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(TypeaheadResource.class);

	@GET
	@ApiOperation(value = "Get Entities By Name", notes = "get entity by names", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.SearchProcessorResponseDTO.class)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response getEntitiesByName(
			@Context HttpServletRequest request,
			@QueryParam(value = "name") @ApiParam(name = "name", required = false, value = "name") final String name,
			@QueryParam(value = "type") @ApiParam(name = "type", required = false, value = "type") final String type) {
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			
			@Override
			public Object call() {

				ProcessorConfigurationService processor = new ProcessorConfigurationService();
				GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
				if (type.equals("mailbox")) {

					LOG.debug("Get Mailbox Names");
					searchFilter.setMbxName(name);
					return processor.getMailBoxNames(searchFilter);
				} else if (type.equals("processor")) {

					LOG.debug("Get processor Names");
					searchFilter.setProcessorName(name);
					return processor.getProcessorNames(searchFilter);
				} else {
					
					LOG.debug("Get profile Names");
					searchFilter.setProfileName(name);
					return processor.getProfileNames(searchFilter);
				}
			}
		};
		worker.actionLabel = "TypeaheadResource.getEntitiesByName()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);

		// hand the delegate to the framework for calling
		return process(request, worker);
	}

	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		
		return new DefaultAuditStatement(Status.ATTEMPT, actionLabel,
				PCIV20Requirement.PCI10_2_5, PCIV20Requirement.PCI10_2_2,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
				HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
	}

}
