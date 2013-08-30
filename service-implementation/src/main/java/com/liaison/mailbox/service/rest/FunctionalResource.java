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

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;

/**
 * This is the gateway for mailbox functional services.
 *
 * @author veerasamyn
 */
@Path("/functional")
public class FunctionalResource {

	private static final Logger LOG = LoggerFactory.getLogger(FunctionalResource.class);

    @Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger failureCounter     = new AtomicInteger(0);

    @Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public FunctionalResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
     * REST Method  to trigger mailbox profile.
     *
     * @return 		  Response Object
     */
	@POST
    @Path("/triggerProfile")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response triggerProfile(@Context HttpServletRequest request) {

    	return Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN).entity("Trigger profile not yet implemented.").build();
    }

}
