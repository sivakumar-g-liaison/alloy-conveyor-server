package com.liaison.mailbox.service.rest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.mailbox.service.core.MailboxTenancyKeyService;
import com.liaison.mailbox.service.dto.configuration.response.GetTenancyKeysResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("v1/mailbox/tenancyKeys/")
@Api(value = "v1/mailbox/tenancyKeys", description = "gateway to retrieve all tenancy keys of acl manifest in request")
public class MailboxTenancyKeyResource extends BaseResource {

	private static final Logger LOG = LogManager.getLogger(MailboxTenancyKeyResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);
	
	public MailboxTenancyKeyResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * REST method to retrieve all domains from acl-manifest present in request header.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Tenancy Keys present in acl-manifest in header",
	notes = "returns tenancy keys present in acl-manifest header",
	position = 1,
	response = com.liaison.mailbox.service.dto.configuration.response.GetTenancyKeysResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({
		@ApiResponse( code = 500, message = "Unexpected Service failure." )
	})
	@AccessDescriptor(accessMethod = "retrieveTenancyKeys")
	public Response retrieveTenancyKeys(@Context HttpServletRequest request) {
		
		//Audit LOG the Attempt to retrieve tenancykeys
		auditAttempt("retrieveTenancyKeys");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
	
		try {
			
			// retrieving acl manifest from header
			LOG.info("Retrieving acl manifest json from request header");
			String manifestJson = request.getHeader("acl-manifest");
			String decodedManifestJson = MailBoxUtil.getDecodedManifestJson(manifestJson);
			
			GetTenancyKeysResponseDTO serviceResponse = null;
			MailboxTenancyKeyService mailboxTenancyKey = new MailboxTenancyKeyService();
			serviceResponse =  mailboxTenancyKey.getAllTenancyKeysFromACLManifest(decodedManifestJson);
			
			//Audit LOG
			doAudit(serviceResponse.getResponse(), "createMailBox");

			// populate the response body
			return serviceResponse.constructResponse();
			
		} catch (Exception  e) {
			
			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("retrieveTenancyKeys");
		
		}	
		
		// Audit LOG the failure
		auditFailure("retrieveTenancyKeys");
		return returnResponse;
	}
}
