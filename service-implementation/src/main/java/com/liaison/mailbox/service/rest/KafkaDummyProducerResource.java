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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.annotation.AccessDescriptor;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.queue.kafka.Producer;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This is dummy kafka producer.
 * 
 * @author OFS
 */
@AppConfigurationResource
@Path("kafka/dummy/producer")
@Api(value = "kafka/dummy/producer", description = "dummy kafka producer service.")
public class KafkaDummyProducerResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(KafkaDummyProducerResource.class);


    @GET
	@ApiOperation(value = "common response", position = 3, response = com.liaison.mailbox.service.dto.CommonResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    @AccessDescriptor(skipFilter = true)
	public Response dummyKafkaProducer(
			@Context final HttpServletRequest request) {

		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

			@Override
			public Object call() throws IOException {

				LOG.info("dummy kafka producer");
				Producer.produce();
				return marshalResponse(Response.Status.OK.getStatusCode(), MediaType.TEXT_PLAIN, "Produced dummy's");

			}
		};
		worker.actionLabel = "MailboxReadResource.dummyKafkaProducer()";

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
