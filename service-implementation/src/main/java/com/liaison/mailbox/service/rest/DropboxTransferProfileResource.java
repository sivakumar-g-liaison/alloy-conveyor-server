package com.liaison.mailbox.service.rest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.dropbox.authenticator.util.DropboxAuthenticatorUtil;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.dropbox.DropboxFileTransferService;
import com.liaison.mailbox.service.dto.configuration.response.GetTransferProfilesResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;


@Path("dropbox/transferProfiles")
public class DropboxTransferProfileResource extends AuditedResource {
	
	private static final Logger LOG = LogManager.getLogger(DropboxTransferProfileResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public DropboxTransferProfileResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	@GET
	@ApiOperation(value = "get list of transferprofiles",
	notes = "retrieve the list of transferprofiles",
	position = 1,
	response = com.liaison.mailbox.service.dto.configuration.response.DropboxTransferContentResponseDTO.class)

	@ApiResponses({
		@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	public Response getTransferProfiles(@Context final HttpServletRequest serviceRequest) {
		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

				serviceCallCounter.incrementAndGet();

				LOG.debug("Entering getTransferProfiles");
				try {
					Response serviceResponse = null;
					Response authResponse = DropboxAuthenticatorUtil.authenticateAndGetManifest(serviceRequest);
					Map <String, String> responseHeaders = DropboxAuthenticatorUtil.retrieveResponseHeaders(authResponse);
					switch (authResponse.getStatus()) {
					
						case MailBoxConstants.ACL_RETRIVAL_FAILURE_CODE:
							serviceResponse =  authResponse;
							break;
						case MailBoxConstants.AUTH_FAILURE_CODE:
							serviceResponse =  authResponse;
							break;
						case MailBoxConstants.AUTH_SUCCESS_CODE:
							
							DropboxFileTransferService fileTransferService = new DropboxFileTransferService();
							
							// retrieving headers from auth response
							String aclManifest = responseHeaders.get(MailBoxConstants.ACL_MANIFEST_HEADER);
							String aclSignature =responseHeaders.get(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER);
							String aclSignerGuid =responseHeaders.get(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID);
							String token = responseHeaders.get(MailBoxConstants.DROPBOX_AUTH_TOKEN);
							
							GetTransferProfilesResponseDTO getTransferProfilesResponseDTO = fileTransferService.getTransferProfiles(serviceRequest, aclManifest);
							String responseBody = MailBoxUtil.marshalToJSON(getTransferProfilesResponseDTO);
							// response message construction
							ResponseBuilder builder = Response.ok().header(MailBoxConstants.ACL_MANIFEST_HEADER, aclManifest)
									.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, aclSignature).header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID, aclSignerGuid)
									.header(MailBoxConstants.DROPBOX_AUTH_TOKEN, token)
									.type(MediaType.APPLICATION_JSON)
									.entity(responseBody)
									.status(Response.Status.OK);
							LOG.debug("Exit from getStagedFiles service.");
							serviceResponse = builder.build();	
					}						
					
					return serviceResponse ;				
				
				} catch (MailBoxServicesException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				} catch (IOException | JAXBException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "DropboxFileTransferResource.getTransferProfiles()";

		// hand the delegate to the framework for calling
		try {
			return handleAuditedServiceRequest(serviceRequest, worker);
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
