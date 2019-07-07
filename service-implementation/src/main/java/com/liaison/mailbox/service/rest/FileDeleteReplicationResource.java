/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.rest;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.framework.RuntimeProcessResource;
import com.liaison.mailbox.service.core.FileDeleteReplicationService;

import com.liaison.mailbox.service.queue.kafka.KafkaMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import java.io.IOException;

/**
 * Created by VNagarajan on 11/22/2016.
 */
@RuntimeProcessResource
@Path("/process/wipe")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class FileDeleteReplicationResource extends AuditedResource {

    private static final Logger logger = LogManager.getLogger(FileDeleteReplicationResource.class);

    @POST
    @AccessDescriptor(skipFilter = true)
    public Response handleFileDeleteReplication(@Context HttpServletRequest request) {

        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() throws JAXBException {
                String requestString;
                try {
                    requestString = getRequestBody(request);
                    logger.info(requestString);
                    FileDeleteReplicationService deleteService = new FileDeleteReplicationService();
                    deleteService.inactivateStageFileAndUpdateLens(JAXBUtility.unmarshalFromJSON(requestString, KafkaMessage.class));
                } catch (IOException e) {
                    throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage(), e);
                }
                return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
            }
        };
        
        worker.actionLabel = "FileDeleteReplicationResource.handleFileDeleteReplication()";
        
        // hand the delegate to the framework for calling
        return process(request, worker);
    }

    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(AuditStatement.Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
                PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
                HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }
}