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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.util.StreamUtil;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.core.ProfileConfigurationService;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProfileRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.FileInfoDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseMailBoxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.ReviseProcessorRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProfileResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeActivateProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.DeactivateMailboxProfileLinkResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.ReviseProcessorResponseDTO;
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
public class MailBoxConfigurationResource {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public MailBoxConfigurationResource() {
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

		serviceCallCounter.addAndGet(1);

		Response returnResponse;

		try {

			DeActivateMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

			// deactivates existing mailbox
			serviceResponse = mailbox.deactivateMailBox(guid);

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
	public Response readMailBox(@PathParam(value = "id") String guid) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;

		try {

			// add the new profile details
			GetMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			serviceResponse = mailbox.getMailBox(guid);

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
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

		serviceCallCounter.addAndGet(1);

		Response returnResponse;
		try {

			// add the new profile details
			GetProfileResponseDTO serviceResponse = null;
			ProfileConfigurationService mailbox = new ProfileConfigurationService();
			serviceResponse = mailbox.getProfiles();

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}

		return returnResponse;

	}

	/**
	 * REST method to deactivate a profile - mailbox link.
	 * 
	 * @param mailGuid
	 *            The id of the mailbox
	 * @param linkGuid
	 *            The id of the mailbox scheduler profile link
	 * @return Response Object
	 */
	@DELETE
	@Path("/{mailboxguid}/profile/{mailboxprofilelinkguid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deactivateMailboxProfileLink(@PathParam(value = "mailboxguid") String mailGuid,
			@PathParam(value = "mailboxprofilelinkguid") String linkGuid) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;

		try {

			// deactivate the profile details
			DeactivateMailboxProfileLinkResponseDTO serviceResponse = null;
			ProfileConfigurationService profile = new ProfileConfigurationService();
			serviceResponse = profile.deactivateMailboxProfileLink(mailGuid, linkGuid);

			// populate the response body
			return serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
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

			// populate the response body
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "processorConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
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
	public Response deleteProcessor(@PathParam(value = "mailboxid") String mailboxguid,
			@PathParam(value = "processorid") String guid) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;

		try {

			// add the new profile details
			DeActivateProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Deactivating processor
			serviceResponse = mailbox.deactivateProcessor(mailboxguid, guid);
			// Constructing response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
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
	public Response getProcessor(@PathParam(value = "mailboxid") String mailboxguid,
			@PathParam(value = "processorid") String guid) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;

		try {

			GetProcessorResponseDTO serviceResponse = null;
			ProcessorConfigurationService mailbox = new ProcessorConfigurationService();
			// Gets processor details.
			serviceResponse = mailbox.getProcessor(mailboxguid, guid);
			// constructs response.
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "Get Processor failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
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
			// constructs response
			returnResponse = serviceResponse.constructResponse();

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailboxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}

		return returnResponse;
	}

	/**
	 * Rest method to search the mailbox based on the given query parameters. If both are empty it
	 * returns all mailboxes.
	 * 
	 * @param mbxName
	 *            The mailbox name should be searched
	 * @param profileName
	 *            The profile name should be searched
	 * @return The Response
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMailBox(@QueryParam(value = "name") String mbxName,
			@QueryParam(value = "profile") String profileName) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;

		try {

			// search the mailbox from the given details
			SearchMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			serviceResponse = mailbox.searchMailBox(mbxName, profileName);

			returnResponse = serviceResponse.constructResponse();
		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using
			// above code
			returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}

		return returnResponse;

	}
	
	@GET

	@Path("/listFile")

	@Produces(MediaType.APPLICATION_JSON)

	public Response getFileList() {

      serviceCallCounter.addAndGet(1);
      Response returnResponse;
      
      try {

            File f = new File(System.getenv().get("USERPROFILE") + File.separator + "net");
            MailBoxConfigurationService mailbox = new MailBoxConfigurationService();

            FileInfoDTO info = mailbox.getFileDetail(f);

            List<FileInfoDTO> infos = new ArrayList<FileInfoDTO>();
            infos.add(info);
            String onfo = MailBoxUtility.marshalToJSON(infos);	 

            return Response.ok(onfo).header("Content-Type", MediaType.APPLICATION_JSON).build();
      } catch (Exception e) {
    	  
            int f = failureCounter.addAndGet(1);
            String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
            LOG.error(errMsg, e);
            returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
      }
      return returnResponse;
	}
}
