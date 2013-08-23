/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.service.core.MailBox;

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

	/**
	 * REST Method to insert the analytic profile details.
	 *
	 * @return JSON meta data of file groups
	 */
	@Path("/addConfig")
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	public Response addConfig(@QueryParam("profile") String profile,
			@QueryParam("name") String name,
			@QueryParam("url") String url,
			@QueryParam("id") String id) {
		//TODO change to json input.
		try {

			MailBox mailBox = new MailBox();
			mailBox.insertProfileComponents(name, profile, url, id);
			LOGGER.info("Configuration added successfully for profile ::{}", profile);
		} catch (Exception e) {
			LOGGER.error("Error in directory sweeping.", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
		return null;
	}
}
