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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.response.GetTrustStoreResponseDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the mailbox upload self signed trust store services.
 * 
 * @author OFS
 */
@Path("mailbox/uploadSelfSigned")
@Api(value = "mailbox/uploadSelfSigned", 
description = "Gateway for the mailbox upload self signed trust store services.")
public class MailBoxUploadSelfSignedTrustStoreResource extends BaseResource{
	
	
	private static final Logger LOG = LogManager.getLogger(MailBoxUploadSelfSignedTrustStoreResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);	
	

	public MailBoxUploadSelfSignedTrustStoreResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * 
	 * REST method for uploading Self Signed TrustStore
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Upload TrustStore",
			notes = "upload Self Signed TrustStore",
			position = 1,
			response = com.liaison.mailbox.service.dto.configuration.response.GetTrustStoreResponseDTO.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "uploadSelfSignedTrustStore")
	public Response uploadSelfSignedTrustStore() {

		// Audit LOG the Attempt to uploadSelfSignedTrustStore
		auditAttempt("uploadSelfSignedTrustStore");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		
		try {

			// add the new profile details
			GetTrustStoreResponseDTO serviceResponse = null;
			ProcessorConfigurationService processor = new ProcessorConfigurationService();
			serviceResponse = processor.uploadSelfSignedTrustStore();

			// Audit LOG
			doAudit(serviceResponse.getResponse(), "uploadSelfSignedTrustStore");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxUploadSelfSignedTrustStoreResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("uploadSelfSignedTrustStore");
		}
		return returnResponse;

	}

}
