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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.acl.annotation.ResourceType;
import com.liaison.commons.util.StreamUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.HTTPServerListenerService;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.FileInfoDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.SearchMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetPropertiesValueResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetTrustStoreResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ServerListenerResponseDTO;
import com.liaison.mailbox.service.dto.ui.GetProfileResponseDTO;
import com.liaison.mailbox.service.dto.ui.SearchMailBoxResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * This is the gateway for the mailbox configuration services.
 * 
 * @author veerasamyn
 */
@Path("v1/mailbox")
@Api(value = "v1/mailbox", description = "Gateway for the mailbox configuration services.")
public class MailBoxConfigurationResource extends BaseResource {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public MailBoxConfigurationResource() throws IOException {

		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST method to initiate mailbox creation.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Enterprise", notes = "create a new mailbox", position = 1)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "createMailBox")
	public Response createMailBox(@Context HttpServletRequest request) {

		// Audit LOG the Attempt to create a mailbox
		auditAttempt("createMailBox");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;
		AddMailboxRequestDTO serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtility.unmarshalFromJSON(requestString, AddMailboxRequestDTO.class);

			// add the new profile details
			AddMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			// creates new mailbox
			serviceResponse = mailbox.createMailBox(serviceRequest);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "createMailBox");

			// populate the response body
			return serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e + e.getMessage();
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("createMailBox");
		return returnResponse;

	}

	/**
	 * REST method to update existing mailbox.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@PUT
	@ApiOperation(value = "Update Enterprise", notes = "update details of existing mailbox", position = 2)
	@Path("/{id}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "reviseMailBox")
	public Response reviseMailBox(@Context HttpServletRequest request, 
			@PathParam(value = "id") @ApiParam(name="id", required=true, value="mailbox guid") String guid) {

		// Audit LOG the Attempt to revise a mailbox
		auditAttempt("reviseMailBox");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;
		ReviseMailBoxRequestDTO serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtility.unmarshalFromJSON(requestString, ReviseMailBoxRequestDTO.class);

			ReviseMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			// updates existing mailbox
			serviceResponse = mailbox.reviseMailBox(serviceRequest, guid);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "reviseMailBox");

			// populate the response body
			return serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("reviseMailBox");
		return returnResponse;
	}

	/**
	 * REST method to delete a mailbox.
	 * 
	 * @param guid
	 *            The id of the mailbox
	 * 
	 * @return Response Object
	 */
	@DELETE
	@ApiOperation(value = "Delete Enterprise", notes = "delete a mailbox", position = 3)
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "deactivateMailBox")
	public Response deactivateMailBox(
			@PathParam(value = "id") @ApiParam(name="id", required=true, value="mailbox guid") String guid) {

		// Audit LOG the Attempt to deactivate a mailbox
		auditAttempt("deactivateMailBox");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			DeActivateMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			// deactivates existing mailbox
			serviceResponse = mailbox.deactivateMailBox(guid);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "deactivateMailBox");

			// populate the response body
			return serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("deactivateMailBox");
		return returnResponse;

	}

	/**
	 * REST method to retrieve a mailbox details.
	 * 
	 * @param guid
	 *            The id of the mailbox
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Enterprise Details", notes = "returns details of a valid mailbox", position = 4)
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "readMailBox")
	public Response readMailBox(@PathParam(value = "id") @ApiParam(name="id", required=true, value="mailbox guid") String guid, 
			@QueryParam(value = "serviceInstanceId") String serviceInstanceId,
			@QueryParam(value = "addServiceInstanceIdConstraint") boolean addConstraint) {

		// Audit LOG the Attempt to read mailbox
		auditAttempt("readMailBox");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			// add the new profile details
			GetMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			serviceResponse = mailbox.getMailBox(guid, serviceInstanceId, addConstraint);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "readMailBox");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("readMailBox");
		}
		return returnResponse;

	}

	/*
	 * @GET
	 * 
	 * @Path("/{id}/processorSID/{sid}")
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * getMailBoxByGuidAndServiceInstId(@PathParam(value = "id") String guid,
	 * 
	 * @PathParam(value = "sid") Integer serviceInstId) {
	 * 
	 * serviceCallCounter.addAndGet(1);
	 * 
	 * Response returnResponse;
	 * 
	 * try {
	 * 
	 * // add the new profile details GetMailBoxResponseDTO serviceResponse =
	 * null; MailBoxConfigurationService mailbox = new
	 * MailBoxConfigurationService(); serviceResponse =
	 * mailbox.getMailBoxByGuidAndServiceInstId(guid, serviceInstId);
	 * 
	 * returnResponse = serviceResponse.constructResponse(); } catch (Exception
	 * e) {
	 * 
	 * int f = failureCounter.addAndGet(1); String errMsg =
	 * "MailBoxConfigurationResource failure number: " + f + "\n" + e;
	 * LOG.error(errMsg, e);
	 * 
	 * // should be throwing out of domain scope and into framework using //
	 * above code returnResponse = Response.status(500).header("Content-Type",
	 * MediaType.TEXT_PLAIN).entity(errMsg).build(); }
	 * 
	 * return returnResponse;
	 * 
	 * }
	 */

	/**
	 * 
	 * REST method for uploading Self Signed TrustStore
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Upload TrustStore", notes = "upload Self Signed TrustStore", position = 5)
	@Path("/uploadSelfSigned")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "uploadSelfSignedTrustStore");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("uploadSelfSignedTrustStore");
		}
		return returnResponse;

	}

	/**
	 * REST method to initiate profile creation.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @return Response Object
	 */
	@POST
	@ApiOperation(value = "Create Profile", notes = "create a new profile", position = 6)
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "createProfile")
	public Response createProfile(@Context HttpServletRequest request) {

		// Audit LOG the Attempt to createProfile
		auditAttempt("createProfile");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;
		AddProfileRequestDTO serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtility.unmarshalFromJSON(requestString, AddProfileRequestDTO.class);

			AddProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService profile = new ProfileConfigurationService();

			// creates new profile
			serviceResponse = profile.createProfile(serviceRequest);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "createProfile");

			// populate the response body
			return serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("createProfile");
		return returnResponse;

	}

	/**
	 * REST method to retrieve all profiles.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "List Profiles", notes = "returns detail information of all the profiles", position = 7)
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "readProfiles")
	public Response readProfiles() {

		// Audit LOG the Attempt to readProfiles
		auditAttempt("readProfiles");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		try {

			// add the new profile details
			GetProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService mailbox = new ProfileConfigurationService();
			serviceResponse = mailbox.getProfiles();

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "readProfiles");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("readProfiles");
		}
		return returnResponse;

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
	@ApiOperation(value = "Create Processor", notes = "create a new processor", position = 8)
	@Path("/{id}/processor")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "createProcessor")
	public Response createProcessor(@Context HttpServletRequest request,
			@PathParam(value = "id") @ApiParam(name="id", required=true, value="processor id") String guid) {

		// Audit LOG the Attempt to createProcessor
		auditAttempt("createProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;
		AddProcessorToMailboxRequestDTO serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtility.unmarshalFromJSON(requestString, AddProcessorToMailboxRequestDTO.class);

			// add the new profile details
			AddProcessorToMailboxResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			serviceResponse = mailbox.createProcessor(guid, serviceRequest);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "createProcessor");

			// populate the response body
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "processorConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("createProcessor");
		}
		return returnResponse;

	}

	/**
	 * REST method to remove a processor details.
	 * 
	 * @param mailboxguid
	 *            The id of the mailbox
	 * @param guid
	 *            The id of the processor
	 * @return Response Object
	 * 
	 */
	@DELETE
	@ApiOperation(value = "Remove Processor", notes = "remove processor details", position = 9)
	@Path("/{mailboxid}/processor/{processorid}")	
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "deleteProcessor")
	public Response deleteProcessor(
			@PathParam(value = "mailboxid") @ApiParam(name="mailboxid", required=true, value="mailbox guid") String mailboxguid, 
			@PathParam(value = "processorid") @ApiParam(name="processorid", required=true, value="processor id") String guid) {

		// Audit LOG the Attempt to deleteProcessor
		auditAttempt("deleteProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			// add the new profile details
			DeActivateProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Deactivating processor
			serviceResponse = mailbox.deactivateProcessor(mailboxguid, guid);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "deleteProcessor");

			// Constructing response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("deleteProcessor");
		}
		return returnResponse;

	}

	/**
	 * REST method to retrieve a mailbox details.
	 * 
	 * @param mailboxguid
	 *            The id of the mailbox
	 * @param guid
	 *            The id of the processor
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Processor Details", notes = "returns detail information of a valid processor", position = 10)
	@Path("/{mailboxid}/processor/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "getProcessor")
	public Response getProcessor(
			@PathParam(value = "mailboxid") @ApiParam(name="mailboxid", required=true, value="mailbox guid") String mailboxguid, 
			@PathParam(value = "processorid") @ApiParam(name="processorid", required=true, value="processor id")  String guid) {

		// Audit LOG the Attempt to getProcessor
		auditAttempt("getProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			GetProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Gets processor details.
			serviceResponse = mailbox.getProcessor(mailboxguid, guid);
			
			//Audit LOG
			doAudit(serviceResponse.getResponse(), "getProcessor");

			// constructs response.
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "Get Processor failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("getProcessor");
		}
		return returnResponse;

	}

	/**
	 * REST method to update existing processor.
	 * 
	 * @param request
	 *            HttpServletRequest, injected with context annotation
	 * @param mailboxguid
	 *            The id of the mailbox
	 * @param guid
	 *            The id of the processor
	 * @return Response Object
	 */
	@PUT
	@ApiOperation(value = "Update Processor", notes = "revise details of valid processor", position = 11)
	@Path("/{mailboxid}/processor/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "reviseProcessor")
	public Response reviseProcessor(@Context HttpServletRequest request, 
			@PathParam(value = "mailboxid")  @ApiParam(name="mailboxid", required=true, value="mailbox guid") String mailboxguid,
			@PathParam(value = "processorid") @ApiParam(name="processorid", required=true, value="processor id") String guid) {

		// Audit LOG the Attempt to reviseProcessor
		auditAttempt("reviseProcessor");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;
		ReviseProcessorRequestDTO serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			serviceRequest = MailBoxUtility.unmarshalFromJSON(requestString, ReviseProcessorRequestDTO.class);

			ReviseProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// updates existing processor
			serviceResponse = mailbox.reviseProcessor(serviceRequest, mailboxguid, guid);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "reviseProcessor");

			// constructs response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("reviseProcessor");
		}
		return returnResponse;
	}

	/**
	 * Rest method to search the mailbox based on the given query parameters. If
	 * both are empty it returns all mailboxes.
	 * 
	 * @param mbxName
	 *            The mailbox name should be searched
	 * @param profileName
	 *            The profile name should be searched
	 * @return The Response
	 */
	@PUT
	@ApiOperation(value = "Search Enterprise", notes = "search a mailbox using given query parameters", position = 12)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "searchMailBox")
	public Response searchMailBox(@Context HttpServletRequest request, @QueryParam(value = "name") String mbxName,
			@QueryParam(value = "profile") String profileName, @QueryParam(value = "hitCounter") String hitCounter) {
		
		//Audit LOG the Attempt to searchMailBox
		auditAttempt("searchMailBox");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		SearchMailboxRequestDTO searchMbxRequest;
		InputStream requestStream;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			searchMbxRequest = MailBoxUtility.unmarshalFromJSON(requestString, SearchMailboxRequestDTO.class);

			// search the mailbox from the given details
			SearchMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			serviceResponse = mailbox.searchMailBox(searchMbxRequest, mbxName, profileName);
			serviceResponse.setHitCounter(hitCounter);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "searchMailBox");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("searchMailBox");
		}
		return returnResponse;

	}
    
	/**
	 * REST method to retrieve a list of files from mailbox.
	 * 
	 * @return The Response Object
	 */
	@GET
	@ApiOperation(value = "List File", notes = "return list of files", position = 13)
	@Path("/listFile")
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "getFileList")
	public Response getFileList() {

		// Audit LOG the Attempt to getFileList
		auditAttempt("getFileList");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {
			String jsFileLocation = MailBoxUtility.getEnvironmentProperties().getString("rootDirectory");
			File file = new File(jsFileLocation);
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			FileInfoDTO info = mailbox.getFileDetail(file);

			List<FileInfoDTO> infos = new ArrayList<FileInfoDTO>();
			infos.add(info);
			String response = MailBoxUtility.marshalToJSON(infos);

			// Audit LOG the success
			auditSuccess("getFileList");
			return Response.ok(response).header("Content-Type", MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("getFileList");
		return returnResponse;
	}
    
	/**
	 * Rest method to retrieve server listener response.
	 * 
	 * @param request
	 *        HttpServletRequest, injected with context annotation
	 * @param folder
	 *           folder of the mailbox 
	 * @param filename
	 *            filename of the mailbox
	 * @return The Response
	 */
	@POST
	@ApiOperation(value = "Server Listener", notes = "listener for http server", position = 14)
	@Path("/serverlistener")
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "httpServerListener")
	public Response httpServerListener(@Context HttpServletRequest request, @HeaderParam(MailBoxConstants.FOLDER_HEADER) String folder,
			@HeaderParam(MailBoxConstants.FILE_NAME_HEADER) String filename) {

		// Audit LOG the Attempt to httpServerListener
		auditAttempt("httpServerListener");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			HTTPServerListenerService service = new HTTPServerListenerService();
			ServerListenerResponseDTO serviceResponse = service.serverListener(requestString, folder, filename);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "httpServerListener");

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("httpServerListener");
		}
		return returnResponse;
	}
    
	/**
	 * Rest method will test directory sweeper.
	 * 
	 * @param request
	 *        HttpServletRequest, injected with context annotation
	 * @return The Response Object.
	 *//*
	@POST
	@ApiOperation(value = "Sweeper", notes = "directory sweeper", position = 15)
	@Path("/sweeper")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response testSweeper(@Context HttpServletRequest request) {

		// Audit LOG the Attempt to testSweeper
		auditAttempt("testSweeper");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			LOG.info("The directory sweeper meta data json : " + new JSONObject(requestString).toString(2));
			// Audit LOG the success
			auditSuccess("testSweeper");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(Messages.SUCCESS.value()).build();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("testSweeper");
		}
		return returnResponse;
	}
    
	*//**
	 * Rest method will test directory sweeper.
	 * 
	 * @param request
	 *        HttpServletRequest, injected with context annotation
	 * @return The Response Object.
	 *//*
	@POST
	@ApiOperation(value = "Test", notes = "test response", position = 16)
	@Path("/test")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response test(@Context HttpServletRequest request) {

		// Audit LOG the Attempt to test
		auditAttempt("test");
		serviceCallCounter.addAndGet(1);

		Response returnResponse;
		try {

			LOG.info("The directory sweeper meta data json ");
			// Audit LOG the success
			auditSuccess("test");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Retry").build();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("test");
		}
		return returnResponse;
	}*/
    
	/**
	 * Rest method to retrieve the Authorization header.
	 * 
	 * @param request
	 *        HttpServletRequest, injected with context annotation
	 * @return The Response Object.
	 */
	@POST
	@ApiOperation(value = "Authorization", notes = "authorization header", position = 17)
	@Path("/basicauth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "basicauth")
	public Response httpServerListener(@Context HttpServletRequest request, @HeaderParam("Authorization") String auth) {

		// Audit LOG the Attempt to httpServerListener
		auditAttempt("httpServerListener");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			LOG.info("Successfully retrieved the auth header : {} ", auth);
			// Audit LOG the success
			auditSuccess("httpServerListener");
			returnResponse = Response.status(200).header("Content-Type", MediaType.TEXT_PLAIN).entity(auth).build();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("httpServerListener");
		}
		return returnResponse;
	}

	/**
	 * REST method to search profiles by profile Name.
	 * 
	 * @param profileName
	 *            The profile name should be searched
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Find Profile", notes = "search a profile using given profile name", position = 18)
	@Path("/findprofile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "findProfiles")
	public Response findProfiles(@QueryParam(value = "name") String profileName) {

		// Audit LOG the Attempt to findProfiles
		auditAttempt("findProfiles");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		try {

			// add the new profile details
			GetProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService mailbox = new ProfileConfigurationService();
			serviceResponse = mailbox.searchProfiles(profileName);

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "findProfiles");

			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("findProfiles");
		}
		return returnResponse;
	}
    
	/**
	 * REST method to retrieve list of certificates.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "List Certificates", notes = "returns list of certificates", position = 19)
	@Path("/listCertificates")
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "getCertificatesList")
	public Response getCertificatesList() {

		// Audit LOG the Attempt to getCertificatesList
		auditAttempt("getCertificatesList");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {
			String jsFileLocation = MailBoxUtility.getEnvironmentProperties().getString("certificateDirectory");
			File file = new File(jsFileLocation);
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			FileInfoDTO info = mailbox.getFileDetail(file);

			List<FileInfoDTO> infos = new ArrayList<FileInfoDTO>();
			infos.add(info);
			String response = MailBoxUtility.marshalToJSON(infos);

			// Audit LOG the success
			auditSuccess("getCertificatesList");
			return Response.ok(response).header("Content-Type", MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		// Audit LOG the failure
		auditFailure("getCertificatesList");
		return returnResponse;
	}

	
	/**
	 * REST method to retrieve property file values.
	 * 
	 * @return Response Object
	 */
	@GET
	@ApiOperation(value = "Property File", notes = "returns property file values", position = 20)
	@Path("/getPropertyFileValues")
	@Produces(MediaType.APPLICATION_JSON)
	@AccessDescriptor(accessMethod = "getPropertyFileValues")
	public Response getPropertyFileValues() {

		// Audit LOG the Attempt to getjavaPropertyFileValues
		auditAttempt("getjavaPropertyFileValues");

		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {
			GetPropertiesValueResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			serviceResponse = mailbox.getValuesFromPropertiesFile();

			//Audit LOG
			doAudit(serviceResponse.getResponse(), "getjavaPropertyFileValues");
			
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
			// Audit LOG the failure
			auditFailure("getjavaPropertyFileValues");
		}
		return returnResponse;
	}
}
