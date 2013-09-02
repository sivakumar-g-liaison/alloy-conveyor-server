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

import java.io.InputStream;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.StreamUtil;
import com.liaison.mailbox.grammer.GrammerDictionary;
import com.liaison.mailbox.grammer.dto.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorComponentService;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetMailBoxResponseDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;

/**
 * This is the gateway for the mailbox configuration services.
 *
 * @author veerasamyn
 */
@Path("/mailbox")
public class MailBoxConfigurationResource {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationResource.class);

    @Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger failureCounter     = new AtomicInteger(0);

    @Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public MailBoxConfigurationResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}

	/**
	 * REST method to initiate mailbox creation.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@PUT
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

			String marshallingMediaType = null;
			if (requestString.startsWith("<")) {
				serviceRequest = JAXBUtility.unmarshalFromXML(requestStream, GrammerDictionary.getEntityArray());
				marshallingMediaType = MediaType.APPLICATION_XML;
			} else {
				serviceRequest = JAXBUtility.unmarshalFromJSON(requestString, AddMailboxRequestDTO.class);
				marshallingMediaType = MediaType.APPLICATION_JSON;
			}

			//add the new profile details
			AddMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			serviceResponse = mailbox.createMailBox(serviceRequest);

			//populate the response body
			return serviceResponse.constructResponse(marshallingMediaType);

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using above code
            returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}

		return returnResponse;
		
    }

	/**
	 * REST method to update existing mailbox. 
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response updateMailBox(@Context HttpServletRequest request) {
		
		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Update Mailbox not yet implemented.").build();

    }

	/**
	 * REST method to delete a mailbox. 
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response deleteMailBox(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Delete Mailbox not yet implemented.").build();

    }

	/**
	 * REST method to retrieve a mailbox details. 
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response readMailBox(@PathParam(value="id") String guid) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;

		try {

			String marshallingMediaType = MediaType.APPLICATION_JSON;

			//add the new profile details
			GetMailBoxResponseDTO serviceResponse = null;
			MailBoxConfigurationService mailbox = new MailBoxConfigurationService();
			serviceResponse = mailbox.getMailBox(guid);

			//populate the response body
			String responseBody;
			if (MediaType.APPLICATION_XML.equals(marshallingMediaType)) {
				responseBody = JAXBUtility.marshalToXML(serviceResponse);
				returnResponse = Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
			} else {
				responseBody = JAXBUtility.marshalToJSON(serviceResponse);
				returnResponse = Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
			}

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "MailBoxConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using above code
            returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}

		return returnResponse;
		
    }

	/**
	 * REST method to initiate profile creation.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@PUT
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response createProfile(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Create Profile not yet implemented.").build();

    }

	/**
	 * REST method to update a existing profile. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@POST
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Update profile not yet implemented.").build();

    }

	/**
	 * REST method to delete a profile. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@DELETE
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response deleteProfile(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Delete Profile not yet implemented.").build();

    }

	/**
	 * REST method to retrieve a profile details. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@GET
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response readProfile(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Read Profile not yet implemented.").build();

    }

	/**
	 * REST method to associate existing profiles to mailbox. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@POST
	@Path("/linkprofiles")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response linkProfiles(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Link profiles not yet implemented.").build();

    }

	/**
	 * REST method to unlink profiles from mailbox. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@DELETE
	@Path("/linkprofiles")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response unlinkProfiles(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Unlik profiles not yet implemented.").build();

    }

	/**
	 * REST method to add the processors to profile.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@PUT
	@Path("/profile/linkprocessors")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response addProcessorsToProfile(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Add processors to profile not yet implemented.").build();

    }

	/**
	 * REST method to remove processors from profile. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@DELETE
	@Path("/profile/linkprocessors")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response removeProcessorsFromProfile(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Remove processors from profile not yet implemented.").build();

    }

	/**
	 * REST method to retrieve a processor details from profile. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@GET
	@Path("/profile/linkprocessors")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response readProcessorsFromProfile(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Read processors from profile not yet implemented.").build();

    }

	/**
	 * REST method to add a processor.
	 * 
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@PUT
	@Path("/processor")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response addProcessor(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Add processor not yet implemented.").build();

    }

	/**
	 * REST method to update a processor. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@POST
	@Path("/processor")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response updateProcessor(@Context HttpServletRequest request) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;
		InputStream requestStream;
		AddProcessorToMailboxRequestDTO serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			String marshallingMediaType = null;
			if (requestString.startsWith("<")) {
				serviceRequest = JAXBUtility.unmarshalFromXML(requestStream, GrammerDictionary.getEntityArray());
				marshallingMediaType = MediaType.APPLICATION_XML;
			} else {
				serviceRequest = JAXBUtility.unmarshalFromJSON(requestString, AddProcessorToMailboxRequestDTO.class);
				marshallingMediaType = MediaType.APPLICATION_JSON;
			}

			//add the new profile details
			AddProcessorToMailboxResponseDTO serviceResponse = null;
			ProcessorComponentService mailbox = new ProcessorComponentService();
			serviceResponse = mailbox.insertProcessorComponents(serviceRequest);

			//populate the response body
			String responseBody;
			if (MediaType.APPLICATION_XML.equals(marshallingMediaType)) {
				responseBody = JAXBUtility.marshalToXML(serviceResponse);
				returnResponse = Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
			} else {
				responseBody = JAXBUtility.marshalToJSON(serviceResponse);
				returnResponse = Response.ok(responseBody).header("Content-Type", MediaType.APPLICATION_JSON).build();
			}

		} catch (Exception e) {

			int f = failureCounter.addAndGet(1);
			String errMsg = "ProfileConfigurationResource failure number: " + f + "\n" + e;
			LOG.error(errMsg, e);

			// should be throwing out of domain scope and into framework using above code
            returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}

		return returnResponse;

    }

	/**
	 * REST method to delete a processor. 
	 *
	 * @param request HttpServletRequest, injected with context annotation
	 * @return 		  Response Object
	 */
	@DELETE
	@Path("/processor")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response deleteProcessor(@Context HttpServletRequest request) {

		return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Delete processor not yet implemented.").build();

    }

}
