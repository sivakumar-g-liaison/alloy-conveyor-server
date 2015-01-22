package com.liaison.mailbox.service.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

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
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.dropbox.authenticator.util.DropboxAuthenticatorUtil;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dropbox.DropboxStagedFilesService;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/dropbox/stagedFiles/{stagedFileId}")
public class DropboxStagedFileDownloadResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(DropboxStagedFileDownloadResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	protected static final String CONFIGURATION_MAX_REQUEST_SIZE = "com.liaison.servicebroker.sync.max.request.size";

	public DropboxStagedFileDownloadResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	@GET
	@ApiOperation(value = "download staged file",
	notes = "download staged file",
	position = 1)

	@ApiResponses({
		@ApiResponse(code = 500, message = "Unexpected Service failure.")
	})
	public Response downloadStagedFile(@Context final HttpServletRequest serviceRequest, 
			@PathParam(value = "stagedFileId") @ApiParam(name = "stagedfileid", required = true, value = "staged file id") final String stagedFileId) {
		
		//create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws Exception {

				serviceCallCounter.incrementAndGet();

				LOG.debug("Entering into download staged file service");
				try {
					Response serviceResponse = null;
					validateRequestSize(serviceRequest);
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
							DropboxStagedFilesService stagedFileService = new DropboxStagedFilesService();
							if (StringUtil.isNullOrEmptyAfterTrim(stagedFileId)) {
								throw new MailBoxServicesException("Staged file id is Mandatory", Response.Status.BAD_REQUEST);
							}
							
							// retrieving headers from auth response
							String aclManifest = responseHeaders.get(MailBoxConstants.ACL_MANIFEST_HEADER);
							String aclSignature =responseHeaders.get(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER);
							String aclSignerGuid =responseHeaders.get(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID);
							String token = responseHeaders.get(MailBoxConstants.DROPBOX_AUTH_TOKEN);
							
							List<TenancyKeyDTO> tenancyKeys = MailBoxUtil.getTenancyKeysFromACLManifest(aclManifest);
							if (tenancyKeys.isEmpty()) {
								LOG.error("retrieval of tenancy key from acl manifest failed");
								throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
							}
							
							List<String> tenancyKeysArray = new ArrayList<String>();
							
							for (TenancyKeyDTO tenancyKeyDTO : tenancyKeys) {
								tenancyKeysArray.add(tenancyKeyDTO.getGuid());
							}
							
							//validate file id belongs to any user organisation
							String spectrumUrl = stagedFileService.validateIfFileIdBelongsToAnyOrganisation(stagedFileId, tenancyKeysArray);
							if(spectrumUrl == null) {
								throw new MailBoxServicesException("Given staged file id does not belong to any user organisation.", Response.Status.BAD_REQUEST);
							} 
							
							//getting the file stream from spectrum for the given file id
							InputStream payload = StorageUtilities.retrievePayload(spectrumUrl);
							
							// response message construction
							ResponseBuilder builder = Response.ok().header(MailBoxConstants.ACL_MANIFEST_HEADER, aclManifest)
									.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, aclSignature).header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID, aclSignerGuid)
									.header(MailBoxConstants.DROPBOX_AUTH_TOKEN, token)
									.type(MediaType.APPLICATION_OCTET_STREAM)
									.entity(payload)
									.status(Response.Status.OK);
							LOG.debug("Exit from download staged file service.");
							return builder.build();	
					}
					return serviceResponse;		
				} catch (MailBoxServicesException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException(e.getMessage());
				}
			}
		};
		worker.actionLabel = "DropboxStagedFileDownloadResource.downloadStagedFile()";

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
	
	/**
	 * This method will validate the size of the request.
	 *
	 * @param request
	 *            The HttpServletRequest
	 */
	protected void validateRequestSize(HttpServletRequest request) {
		long contentLength = request.getContentLength();
		DecryptableConfiguration config = LiaisonConfigurationFactory
				.getConfiguration();
		int maxRequestSize = config.getInt(CONFIGURATION_MAX_REQUEST_SIZE);

		if (contentLength > maxRequestSize) {
			throw new RuntimeException("Request has content length of "
					+ contentLength
					+ " which exceeds the configured maximum size of "
					+ maxRequestSize);
		}
	}
}
