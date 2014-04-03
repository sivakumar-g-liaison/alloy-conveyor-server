/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.liaison.service.resources.examples;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.service.exceptions.examples.JSONParseException;
import com.liaison.service.exceptions.examples.UnexpectedNameException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * HelloWorldResource
 *
 * <P>Simple HelloWorld REST service example
 *
 * <P>For dynamically described endpoints, @see com.liaison.framework.dynamic.DynamicServicesServlet
 *
 * @author Robert.Christian
 * @version 1.0
 */
@Api(value="v1/hello", description="hello world resource") //swagger resource annotation
@Path("v1/hello")
public class HelloWorldResource {


    private static final Logger logger = LogManager.getLogger(HelloWorldResource.class);

    /**
     * example of returns a a string of greeting.
     * 
     * @param name
     * @return The Response Object
     */
    @ApiOperation(value="hello to given name", notes="this typically returns a string of greeting")
    @Path("/to/{name}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response helloTo(
    		@ApiParam(value="name of the person who is to be greeted", required=true)
    		@PathParam("name") String name) {
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello " + name + "!");

            return Response.ok(response.toString()).build();
        } catch (JSONException e) {

            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     *  example of returns a well known programming trope.
     * 
     * @return The Response Object
     */
    @ApiOperation(value="hello to the world", notes="this returns a well known programming trope")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response hello() {
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello world!");
            return Response.ok(response.toString()).build();
        } catch (JSONException e) {

            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * example of error throwing and handling with auditing
     * 
     * 
     * @param name
     * @return The Response Object
     */
    @Path("error/{name}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response errorTo(@PathParam("name") String name) {
        JSONObject response = new JSONObject();
        if (name != null && name.equalsIgnoreCase("robert")) {
	        try {
	            response.put("Message", "Hello " + name + "!");
	            
	            //Note this is not necessary when using modern framework, but provided only for reference simplicity
	            logger.info(new DefaultAuditStatement(Status.SUCCEED,"SUCESS"));
	            
	            return Response.ok(response.toString()).build();	            
	        } catch (JSONException e) {
	        	throw logger.throwing(new JSONParseException("Wrong exception, fix this", e));
	        }
        }        
    	throw logger.throwing(new UnexpectedNameException("Wrong exception, fix this", javax.ws.rs.core.Response.Status.BAD_REQUEST));
    }
        

}
