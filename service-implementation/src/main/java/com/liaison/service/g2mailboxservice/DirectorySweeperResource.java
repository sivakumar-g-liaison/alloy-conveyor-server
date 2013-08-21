/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.service.g2mailboxservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.service.g2mailboxservice.core.DirectorySweeper;
import com.liaison.service.g2mailboxservice.core.JavaScriptExecutor;
import com.liaison.service.g2mailboxservice.core.dto.MetaDataDTO;
import com.liaison.service.g2mailboxservice.core.util.MailBoxSweeperUtil;
import com.liaison.service.g2mailboxservice.pluggable.DirectorySweeperTaskHandler;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;

/**
 *
 * DirectorySweeperResource. This is the gateway for the directory sweeper service
 *
 *
 * @author veerasamyn
 * @version 1.0
 */

@Path("v1/sweepdirectories")
public class DirectorySweeperResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeperResource.class);

    @Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger FAILURE_COUNTER = new AtomicInteger(0);

    @Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger SERVICE_CALL_COUNTER = new AtomicInteger(0);

    /**
     * Default Constructor.
     */
    public DirectorySweeperResource() {
        DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
    }

    /**
     * REST Method  to initiate directory sweeper process , right now it uses a hard coded path, 
     * this will be later modified to pull the folders to sweep from DB
     *
     * @return JSON meta data of file groups
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response sweepDirectory() {

        SERVICE_CALL_COUNTER.addAndGet(1);

        JSONObject response = new JSONObject();
        DirectorySweeper dirSweeper = new DirectorySweeper();

        try {

            List<java.nio.file.Path> files = dirSweeper.sweepDirectory("C:\\MailBoxRoot\\Boeing-INBOX", false, false, null);
            List<java.nio.file.Path> sweepList = new ArrayList<>(files);
            // Create file groups calling Java script - This will always be called for now .
            //After DB integration  will be optionally called based on the DB configurations.
            //The grouping right now is based on file name patterns. 
            //This is just to prove the files can be grouped into logical work unit
            JavaScriptExecutor jsexecutor = new JavaScriptExecutor();
            List<List<java.nio.file.Path>> fileGroups = jsexecutor.groupFiles(files);  
            
            MetaDataDTO metaData = new MetaDataDTO(fileGroups);
            String jsonResponse = MailBoxSweeperUtil.convertObjectToJson(metaData);  
            dirSweeper.markAsSweeped(sweepList);
            LOGGER.info("Returns json response.{}",new JSONObject(jsonResponse).toString(2));
            response.put("sweepresults", new JSONObject(jsonResponse));
            return Response.ok(response.toString()).build();
        } catch (Exception e) {

            FAILURE_COUNTER.addAndGet(1);
            LOGGER.error("Error in directory sweeping.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * REST Method  to initiate directory sweeper process , 
     * the directory to sweep should be passed on as a request parameter path.
     *
     * @return JSON meta data of file groups
     */
    @Path("/dynamic")
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response sweepDirectory(@QueryParam("path") String path) {

        SERVICE_CALL_COUNTER.addAndGet(1);

        JSONObject response = new JSONObject();
        DirectorySweeperTaskHandler executor;

        try {
        	
        	executor = new DirectorySweeperTaskHandler(path);
        	executor.run();
        	Object executorResponse = executor.getResponse();
        	
            String jsonResponse = (String) executorResponse;
            LOGGER.info("Returns json response.{}",new JSONObject(jsonResponse).toString(2));
            response.put("sweepresults", new JSONObject(jsonResponse));
            return Response.ok(response.toString()).build();
        } catch (Exception e) {

            FAILURE_COUNTER.addAndGet(1);
            LOGGER.error("Error in directory sweeping.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
