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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
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
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.FileInfoDTO;
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
 * This is the gateway for the processor configuration helper services.
 * 
 * @author OFS
 */
@Path("mailbox/listFile")
@Api(value = "mailbox/listFile", 
description = "Gateway for the processor configuration helper.")
public class MailBoxFileResource extends AuditedResource {
	
	private static final Logger LOG = LogManager.getLogger(MailBoxFileResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	

	public MailBoxFileResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * REST method to retrieve a list of files from mailbox.
	 * 
	 * @return The Response Object
	 */
	@GET
	@ApiOperation(value = "List File", notes = "return list of files", position = 1)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
			@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	@AccessDescriptor(accessMethod = "getFileList")
	public Response getFileList() {

		// Audit LOG the Attempt to getFileList
		auditAttempt("getFileList");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {
			String jsFileLocation = MailBoxUtil.getEnvironmentProperties().getString(
					"processor.javascript.root.directory");
			File file = new File(jsFileLocation);
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			FileInfoDTO info = mailbox.getFileDetail(file);

			List<FileInfoDTO> infos = new ArrayList<FileInfoDTO>();
			infos.add(info);
			String response = MailBoxUtil.marshalToJSON(infos);

			// Audit LOG the success
			auditSuccess("getFileList");
			return Response.ok(response).header("Content-Type", MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxFileResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("getFileList");
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
