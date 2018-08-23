/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.liaison.mailbox.rtdm.dao.DatacenterDAO;
import com.liaison.mailbox.rtdm.dao.DatacenterDAOBase;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Class for update the processing dc.
 *
 */
public class DatacenterService {

    private static final String SUCCESS = "Success";
    private static final String PROCESS_DC_DOES_NOT_EXIST = "Process Dc doesn't exist";

    /**
     * Method to update the processing dc in datacenter entity
     * 
     * @param datacenterMap
     * @return response
     */
    public Response updateDatacenter(Map<String, String> datacenterMap) {

        try {

            if (MailBoxUtil.validateProcessDc(datacenterMap)) {

                DatacenterDAO dao = new DatacenterDAOBase();
                dao.updateDatacenter(datacenterMap);
                return Response.status(Response.Status.OK.getStatusCode()).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .entity(SUCCESS).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .entity(PROCESS_DC_DOES_NOT_EXIST).build();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to create the processing dc in datacenter entity
     * 
     * @param datacenterMap
     * @return response
     */
    public Response createDatacenter(Map<String, String> datacenterMap) {

        try {

            if (MailBoxUtil.validateProcessDc(datacenterMap)) {

                DatacenterDAO dao = new DatacenterDAOBase();
                dao.createDatacenter(datacenterMap);
                return Response.status(Response.Status.OK.getStatusCode()).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .entity(SUCCESS).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .entity(PROCESS_DC_DOES_NOT_EXIST).build();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
