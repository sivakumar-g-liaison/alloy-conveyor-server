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

/**
 * This is the gateway processors state monitoring and interrupt.
 *
 * @author OFS
 */

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@AppConfigurationResource
@Path("config/mailbox/processorsearch")
@Api(value = "config/mailbox/processorsearch", description = "Administration of processor services")
public class ProcessorSearchResource extends AuditedResource {

	private static final Logger LOG = LogManager.getLogger(MailBoxConfigurationResource.class);

	@Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger failureCounter = new AtomicInteger(0);

	@Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
	private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

	public ProcessorSearchResource() {
		DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
	}
	
	/**
	 * REST service to get all the processors
	 * 
	 * @param HttpServletRequest
	 * @return Response
	 */
	@GET
	@ApiOperation(value = "Get All Processors", notes = "get all the processors", position = 1, response = com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO.class)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(code = 500, message = "Unexpected Service failure.") })
	public Response getAllProcessors(@Context HttpServletRequest request,
			@QueryParam(value = "page") @ApiParam(name = "page", required = false, value = "page") final String page,
			@QueryParam(value = "pagesize") @ApiParam(name = "pagesize", required = false, value = "pagesize") final String pageSize,
			@QueryParam(value = "sortField") @ApiParam(name = "sortField", required = false, value = "sortField") final String sortField,
			@QueryParam(value = "sortDirection") @ApiParam(name = "sortDirection", required = false, value = "sortDirection") final String sortDirection) {

		// create the worker delegate to perform the business logic
		AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
			@Override
			public Object call() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

				serviceCallCounter.addAndGet(1);

				try {					
					ProcessorConfigurationService processor = new ProcessorConfigurationService();
					GenericSearchFilterDTO searchFilter = new GenericSearchFilterDTO();
					searchFilter.setPage(page);
					searchFilter.setPageSize(pageSize);
					searchFilter.setSortField(sortField);
					searchFilter.setSortDirection(sortDirection);
					// Get all the processors
					return processor.getAllProcessors(searchFilter);
				} catch (IOException | JAXBException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				} catch (SymmetricAlgorithmException e) {
					LOG.error(e.getMessage(), e);
					throw new LiaisonRuntimeException("Unable to Read Request. " + e.getMessage());
				}
			}
		};
		worker.actionLabel = "ProcessorAdminResource.getAllProcessors()";
		worker.queryParams.put(AuditedResource.HEADER_GUID, AuditedResource.MULTIPLE);		

		// hand the delegate to the framework for calling
		try {
			return handleAuditedServiceRequest(request, worker);
		} catch (LiaisonAuditableRuntimeException e) {
			if (!StringUtils.isEmpty(e.getResponseStatus().getStatusCode() + "")) {
				return marshalResponse(e.getResponseStatus().getStatusCode(), MediaType.TEXT_PLAIN, e.getMessage());
			}
			return marshalResponse(500, MediaType.TEXT_PLAIN, e.getMessage());
		}
	}

	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void beginMetricsCollection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endMetricsCollection(boolean success) {
		// TODO Auto-generated method stub
		
	}
}
