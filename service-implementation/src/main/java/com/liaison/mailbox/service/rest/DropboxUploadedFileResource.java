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
import com.liaison.mailbox.service.dropbox.DropboxUploadedFileService;
import com.liaison.mailbox.service.dto.dropbox.UploadedFileDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is the gateway for the Dropbox uploaded files configuration services.
 *
 * @author OFS
 */
@AppConfigurationResource
@Path("config/dropbox/uploadedFiles")
@Api(value = "config/dropbox/uploadedFiles", description = "Gateway for the dropbox services.")
public class DropboxUploadedFileResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(DropboxUploadedFileResource.class);
	
	/**
	 * REST method to add uploaded file.
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 *
	 * @return Response Object
	 */
	@POST	
	@ApiOperation(value = "Added uploaded file", notes = "Add a new upload file entry", position = 1)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiImplicitParams({@ApiImplicitParam(name = "request", value = "Add a new upload file entry")})
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response addUploadedFile(@Context final HttpServletRequest request) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() {

				String requestString;
				try {
					requestString = getRequestBody(request);
					UploadedFileDTO serviceRequest = MailBoxUtil.unmarshalFromJSON(requestString,
							UploadedFileDTO.class);
					//add uploaded file
					DropboxUploadedFileService uploadedFileService = new DropboxUploadedFileService();
					uploadedFileService.addUploadedFile(serviceRequest);					
					return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				}
			}
		};
		worker.actionLabel = "DropboxUploadedFileResource.addUploadedFile()";

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
