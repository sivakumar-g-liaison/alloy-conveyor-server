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

import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.sla.MailboxWatchDogService;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import static com.liaison.mailbox.enums.EntityStatus.ACTIVE;
import static com.liaison.mailbox.enums.EntityStatus.findByCode;

/**
 * This is the gateway to check and update the customer picked up file status in LENS
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("config/mailbox/trigger/status")
@Api(value = "config/mailbox/trigger/status", description = "Updates the customer picked up files status in LENS")
public class MailboxWatchDogResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailboxWatchDogResource.class);
	private static final String TYPE = "type";
	private static final String MAILBOX_STATUS = "mailboxstatus";
	private static final String SWEEPER = "sweeper";
	private static final String CONDITIONALSWEEPER = "conditionalsweeper";

	/**
	 * REST method to validate the sla rules of all mailboxes.
	 * 
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Update the customer picked up file status in LENS",
		notes = "Update the customer picked up file status in LENS",
		position = 23)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	@AccessDescriptor(skipFilter = true)
	public Response updateStatus(@Context final HttpServletRequest request, 
			@QueryParam(value = TYPE) @ApiParam(name = TYPE, required = false, value = "Type of the SLA to be checked.") final String type,
			@QueryParam(value = MAILBOX_STATUS) @ApiParam(name = MAILBOX_STATUS, required = false, value = "status of mailbox") final String mailboxStatus) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				try {
					LOG.debug("Entering into mailbox watchdog resource");

					if (!MailBoxUtil.isEmpty(type) && (SWEEPER.equals(type.toLowerCase()) || CONDITIONALSWEEPER.equals(type.toLowerCase()))) {
                        // validate the sla rules of all mailboxes
                        EntityStatus status = MailBoxUtil.isEmpty(mailboxStatus) ? ACTIVE : findByCode(mailboxStatus.toUpperCase());
                        new MailboxWatchDogService().validateMailboxSLARule(status);
					} else {
						// To validate Mailbox sla for all mailboxes
						new MailboxWatchDogService().pollAndUpdateStatus();
                        new ProcessorExecutionConfigurationService().notifyStuckProcessors();
					}
					return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
				} catch (Exception e) {
					throw new LiaisonRuntimeException("Failed to validate SLA." + e.getMessage(), e);
				}

			}
		};
		worker.actionLabel = "MailboxWatchDogResource.updateStatus()";

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
