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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.mailbox.Version;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "/config/version", description = "Service Version Information")
@Path("/config/version")
public class VersionResource {

    private static final Logger LOG = LogManager.getLogger(VersionResource.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get Version", notes = "Get service version information.", position = 1)
    @AccessDescriptor(skipFilter = true)
    public Response getVersion(@Context final HttpServletRequest request) {
        Version serviceResponse = new com.liaison.mailbox.Version();

        String responseBody;

        Response.ResponseBuilder response;
        try {
            responseBody = JAXBUtility.marshalToJSON(serviceResponse);
            response = Response.status(200).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .entity(responseBody);
        } catch (IOException | JAXBException e) {
            response = Response.status(500).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                    .entity("Response serialization failure.");
            LOG.error(e.getMessage(), e);
        }

        return response.build();
    }
}
