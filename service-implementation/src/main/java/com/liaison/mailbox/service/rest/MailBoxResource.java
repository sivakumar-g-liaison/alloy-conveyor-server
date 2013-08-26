/**
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
import com.liaison.mailbox.grammer.dto.ProfileConfigurationRequest;
import com.liaison.mailbox.grammer.dto.ProfileConfigurationResponse;
import com.liaison.mailbox.service.core.MailBox;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;

/**
 *
 * DirectorySweeperResource. This is the gateway for the directory sweeper service
 *
 *
 * @author ganeshramr
 * @version 1.0
 */

@Path("v1/mailbox")
public class MailBoxResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailBoxResource.class);


    @Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger failureCounter     = new AtomicInteger(0);

    @Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

    /**
     * REST Method  to trigger  mailbox profile sweeping service      * 
     *
     * @return JSON meta data of file groups
     */
    @Path("/triggerProfile/{profile}")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public Response triggerProfile(@PathParam("profile") String profile) {

     try {
    	 
    	 MailBox mailBox = new MailBox();
    	 mailBox.invokeProfileComponents(profile);
        	
        } catch (Exception e) {
            LOGGER.error("Error in directory sweeping.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
	return null;
    }
    
    //TODO offshore - this method should ideally consume a JSON in the post body.
    //This should rather be PUT read http://10.0.2.8/display/ARCH/G2+REST+API+Standard?focusedCommentId=16089354#comment-16089354 including comments
    //Also the method param should be  @Context HttpServletRequest request , refer to keymanager  com.liaison.keymanage.service.rest.KeyUploadResource

	/**
	 * Insert the analytic profile configuration details.
	 *
	 * @return Response object
	 */
    @PUT
	@Path("/addProfileConfig")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response addProfileConfig(@Context HttpServletRequest request) {

		serviceCallCounter.addAndGet(1);

		Response returnResponse;
		InputStream requestStream;
		ProfileConfigurationRequest serviceRequest;

		try {

			requestStream = request.getInputStream();
			String requestString = new String(StreamUtil.streamToBytes(requestStream));

			String marshallingMediaType = null;
			if (requestString.startsWith("<")) {
				serviceRequest = JAXBUtility.unmarshalFromXML(requestStream, GrammerDictionary.getEntityArray());
				marshallingMediaType = MediaType.APPLICATION_XML;
			} else {
				serviceRequest = JAXBUtility.unmarshalFromJSON(requestString, ProfileConfigurationRequest.class);
				marshallingMediaType = MediaType.APPLICATION_JSON;
			}

			//add the new profile details
			ProfileConfigurationResponse serviceResponse = null;
			MailBox mailbox = new MailBox();
			serviceResponse = mailbox.insertProfileComponents(serviceRequest);

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
			LOGGER.error(errMsg, e);

			// should be throwing out of domain scope and into framework using above code
            returnResponse = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity(errMsg).build();
		}

		return returnResponse;
	}
}
