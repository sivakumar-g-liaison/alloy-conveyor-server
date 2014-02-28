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

/**
 * This is the gateway for the mailbox configuration services.
 * 
 * @author veerasamyn
 */
@Path("v1/mailbox")
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMailBox(@Context HttpServletRequest request) {
		
		//Audit LOG the Attempt to create a mailbox
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

			//Audit LOG the success
			auditSuccess("createMailBox");
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
		//Audit LOG the failure
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
	@Path("/{id}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces(MediaType.APPLICATION_JSON)
	public Response reviseMailBox(@Context HttpServletRequest request, @PathParam(value = "id") String guid) {

		//Audit LOG the Attempt to revise a mailbox
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

			//Audit LOG the success
			auditSuccess("reviseMailBox");
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
		//Audit LOG the failure
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
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deactivateMailBox(@PathParam(value = "id") String guid) {

		//Audit LOG the Attempt to deactivate a mailbox
		auditAttempt("deactivateMailBox");
		
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			DeActivateMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			// deactivates existing mailbox
			serviceResponse = mailbox.deactivateMailBox(guid);

			//Audit LOG the success
			auditSuccess("deactivateMailBox");
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
		//Audit LOG the failure
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
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response readMailBox(@PathParam(value = "id") String guid, @QueryParam(value = "serviceInstanceId") String serviceInstanceId,
			@QueryParam(value = "addServiceInstanceIdConstraint") boolean addConstraint) {

		//Audit LOG the Attempt to read mailbox
		auditAttempt("readMailBox");
		
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			// add the new profile details
			GetMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			serviceResponse = mailbox.getMailBox(guid, serviceInstanceId, addConstraint);

			//Audit LOG the success
			auditSuccess("readMailBox");
			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("readMailBox");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Path("/uploadSelfSigned")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadSelfSignedTrustStore() {

		//Audit LOG the Attempt to uploadSelfSignedTrustStore
		auditAttempt("uploadSelfSignedTrustStore");
		
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			// add the new profile details
			GetTrustStoreResponseDTO serviceResponse = null;
			ProcessorConfigurationService processor = new ProcessorConfigurationService();
			serviceResponse = processor.uploadSelfSignedTrustStore();

			//Audit LOG the success
			auditSuccess("uploadSelfSignedTrustStore");
			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("uploadSelfSignedTrustStore");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProfile(@Context HttpServletRequest request) {

		//Audit LOG the Attempt to createProfile
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

			//Audit LOG the success
			auditSuccess("createProfile");
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
		//Audit LOG the failure
		auditFailure("createProfile");
		return returnResponse;

	}

	/**
	 * REST method to retrieve all profiles.
	 * 
	 * @return Response Object
	 */
	@GET
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response readProfiles() {

		//Audit LOG the Attempt to readProfiles
		auditAttempt("readProfiles");
		
		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		try {

			// add the new profile details
			GetProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService mailbox = new ProfileConfigurationService();
			serviceResponse = mailbox.getProfiles();

			//Audit LOG the success
			auditSuccess("readProfiles");
			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("readProfiles");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Path("/{id}/processor")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProcessor(@Context HttpServletRequest request, @PathParam(value = "id") String guid) {

		//Audit LOG the Attempt to createProcessor
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

			//Audit LOG the success
			auditSuccess("createProcessor");
			// populate the response body
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "processorConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("createProcessor");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Path("/{mailboxid}/processor/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteProcessor(@PathParam(value = "mailboxid") String mailboxguid, @PathParam(value = "processorid") String guid) {

		//Audit LOG the Attempt to deleteProcessor
		auditAttempt("deleteProcessor");
		
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			// add the new profile details
			DeActivateProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Deactivating processor
			serviceResponse = mailbox.deactivateProcessor(mailboxguid, guid);
			//Audit LOG the success
			auditSuccess("deleteProcessor");
			// Constructing response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("deleteProcessor");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Path("/{mailboxid}/processor/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProcessor(@PathParam(value = "mailboxid") String mailboxguid, @PathParam(value = "processorid") String guid) {

		//Audit LOG the Attempt to getProcessor
		auditAttempt("getProcessor");
		
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			GetProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Gets processor details.
			serviceResponse = mailbox.getProcessor(mailboxguid, guid);
			// constructs response.
			//Audit LOG the success
			auditSuccess("getProcessor");
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "Get Processor failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("getProcessor");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Path("/{mailboxid}/processor/{processorid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response reviseProcessor(@Context HttpServletRequest request, @PathParam(value = "mailboxid") String mailboxguid,
			@PathParam(value = "processorid") String guid) {

		//Audit LOG the Attempt to reviseProcessor
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
			//Audit LOG the success
			auditSuccess("reviseProcessor");
			// constructs response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("reviseProcessor");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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

			//Audit LOG the success
			auditSuccess("searchMailBox");
			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("searchMailBox");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		return returnResponse;

	}

	@GET
	@Path("/listFile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFileList() {

		//Audit LOG the Attempt to getFileList
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

			//Audit LOG the success
			auditSuccess("getFileList");
			return Response.ok(response).header("Content-Type", MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		//Audit LOG the failure
		auditFailure("getFileList");
		return returnResponse;
	}

	@POST
	@Path("/serverlistener")
	@Produces(MediaType.APPLICATION_JSON)
	public Response httpServerListener(@Context HttpServletRequest request, @HeaderParam(MailBoxConstants.FOLDER_HEADER) String folder,
			@HeaderParam(MailBoxConstants.FILE_NAME_HEADER) String filename) {

		//Audit LOG the Attempt to httpServerListener
	    auditAttempt("httpServerListener");
	    
		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			HTTPServerListenerService service = new HTTPServerListenerService();
			ServerListenerResponseDTO serviceResponse = service.serverListener(requestString, folder, filename);
			//Audit LOG the success
			auditSuccess("httpServerListener");
			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("httpServerListener");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		return returnResponse;
	}

	@POST
	@Path("/sweeper")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response testSweeper(@Context HttpServletRequest request) {
		
		//Audit LOG the Attempt to testSweeper
	    auditAttempt("testSweeper");
	    
		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		InputStream requestStream;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			LOG.info("The directory sweeper meta data json : " + new JSONObject(requestString).toString(2));
			//Audit LOG the success
			auditSuccess("testSweeper");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(Messages.SUCCESS.value()).build();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("testSweeper");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		return returnResponse;
	}

	@POST
	@Path("/test")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response test(@Context HttpServletRequest request) {

		//Audit LOG the Attempt to test
	    auditAttempt("test");
		serviceCallCounter.addAndGet(1);

		Response returnResponse;
		try {

			LOG.info("The directory sweeper meta data json ");
			//Audit LOG the success
			auditSuccess("test");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Retry").build();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("test");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		return returnResponse;
	}

	@POST
	@Path("/basicauth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response httpServerListener(@Context HttpServletRequest request, @HeaderParam("Authorization") String auth) {

		//Audit LOG the Attempt to httpServerListener
	    auditAttempt("httpServerListener");
	    
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {

			LOG.info("Successfully retrieved the auth header : {} ", auth);
			//Audit LOG the success
			auditSuccess("httpServerListener");
			returnResponse = Response.status(200).header("Content-Type", MediaType.TEXT_PLAIN).entity(auth).build();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("httpServerListener");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
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
	@Path("/findprofile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findProfiles(@QueryParam(value = "name") String profileName) {

		//Audit LOG the Attempt to findProfiles
	    auditAttempt("findProfiles");
	    
		serviceCallCounter.addAndGet(1);
		Response returnResponse;
		try {

			// add the new profile details
			GetProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService mailbox = new ProfileConfigurationService();
			serviceResponse = mailbox.searchProfiles(profileName);
			//Audit LOG the success
			auditSuccess("findProfiles");
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			//Audit LOG the failure
			auditFailure("findProfiles");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		return returnResponse;
	}

	@GET
	@Path("/listCertificates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCertificatesList() {

		//Audit LOG the Attempt to getCertificatesList
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

			//Audit LOG the success
			auditSuccess("getCertificatesList");
			return Response.ok(response).header("Content-Type", MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		//Audit LOG the failure
		auditFailure("getCertificatesList");
		return returnResponse;
	}
	
	@GET
	@Path("/globalTrustStoreId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGlobalTrustStoreId() {
		
		//Audit LOG the Attempt to getGlobalTrustStoreId
	    auditAttempt("getGlobalTrustStoreId");
	    
		serviceCallCounter.addAndGet(1);
		Response returnResponse;

		try {
			GetTrustStoreResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			
			serviceResponse = mailbox.getTrustStoreId();
			//Audit LOG the success
			auditSuccess("getGlobalTrustStoreId");
			returnResponse = serviceResponse.constructResponse();
			
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);
			//Audit LOG the failure
			auditFailure("getGlobalTrustStoreId");
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}
		return returnResponse;
	}
}
