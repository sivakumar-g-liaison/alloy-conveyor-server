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
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
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
import com.liaison.fs2.api.exceptions.FS2Exception;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.core.HTTPServerListenerService;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox http server listener services.
 * 
 * @author OFS
 */
@Path("mailbox/serverlistener")
@Api(value = "mailbox/serverlistener", 
description = "Gateway for the mailbox http server listener services.")
public class HTTPServerListenerResource extends AuditedResource {
	
	private static final Logger LOG = LogManager.getLogger(HTTPServerListenerResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	

	public HTTPServerListenerResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * Rest method to retrieve server listener response.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @param folder
	 *            folder of the mailbox
	 * @param filename
	 *            filename of the mailbox
	 * @return The Response
	 */
	@POST
	@ApiOperation(value = "Server Listener",
			notes = "Listener for http server. This method requires folder (location) and file name as header fields.",
			position = 1,
			response = com.liaison.mailbox.service.dto.configuration.response.ServerListenerResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "httpServerListener")
	public Response httpServerListener(@Context final HttpServletRequest request,
			@HeaderParam(MailBoxConstants.FOLDER_HEADER) final String folder,
			@HeaderParam(MailBoxConstants.FILE_NAME_HEADER) final String filename) {

		
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {
				
				serviceCallCounter.addAndGet(1);
				
				String requestString;
				try {
					requestString = getRequestBody(request);
					HTTPServerListenerService service = new HTTPServerListenerService();
					return service.serverListener(requestString, folder, filename);		
				} catch (IOException | FS2Exception | URISyntaxException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				} 		
			}
		};
		worker.actionLabel = "HTTPServerListenerResource.httpServerListener()";

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
