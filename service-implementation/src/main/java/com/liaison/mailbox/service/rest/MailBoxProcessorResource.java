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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox processor configuration services.
 *
 * @author veerasamyn
 */
@Path("mailbox/{mailboxid}/processor")
@Api(value = "mailbox/{mailboxid}/processor",
description = "Gateway for the processor configuration services.")
public class MailBoxProcessorResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxProcessorResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public MailBoxProcessorResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST method to update a processor.
	 *
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @param guid
	 *            The id of the mailbox
	 *
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Processor",
	notes = "create a new processor",
	position = 1,
	response = com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({ @ApiImplicitParam(name = "request", value = "Create new processor", required = true,
			dataType = "com.liaison.mailbox.swagger.dto.request.AddProcessorToMailBoxRequest", paramType = "body") })
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	public Response createProcessor(
			@Context final HttpServletRequest request,
			@PathParam(value = "mailboxid") @ApiParam(name="mailboxid", required=true, value="mailboxid") final String guid,
			@QueryParam(value = "sid") @ApiParam(name = "sid", required = true, value = "Service instance id") final String serviceInstanceId) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				serviceCallCounter.addAndGet(1);

				String requestString;
				try {
					requestString = getRequestBody(request);
					AddProcessorToMailboxRequestDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString, AddProcessorToMailboxRequestDTO.class);
					// create the new Processor
					ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
					return mailbox.createProcessor(guid, serviceRequest, serviceInstanceId);

				} catch (IOException | JAXBException | JSONException  e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				} catch (SymmetricAlgorithmException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to read a mailbox. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "MailBoxProcessorResource.createProcessor()";

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
