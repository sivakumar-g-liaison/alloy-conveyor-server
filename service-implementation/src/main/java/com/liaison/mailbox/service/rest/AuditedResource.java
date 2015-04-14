/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.rest;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.exception.LiaisonUnhandledContainerException;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.stats.StatsConfig;

/**
 * 
 * @author OFS
 * 
 */
public abstract class AuditedResource extends BaseResource {

	// *************************************************
	// *************************************************
	// ************ LOGGING STUFF **************
	private static final Logger logger = LogManager.getLogger(AuditedResource.class);

	// global KPIs
	@Monitor(name = "Global_failureCounter", type = DataSourceType.COUNTER)
	protected static final AtomicInteger globalFailureCounter = new AtomicInteger(0);

	@Monitor(name = "Global_serviceCallCounter", type = DataSourceType.COUNTER)
	protected static final AtomicInteger globalServiceCallCounter = new AtomicInteger(0);

	protected static final StatsTimer globalStatsTimer = new StatsTimer(
			MonitorConfig.builder("Global_statsTimer").build(), new StatsConfig.Builder().build());

	static {
		DefaultMonitorRegistry.getInstance().register(globalStatsTimer);
	}

	/**
	 * This handles auditing around your business logic, then hands the worker off to the KPI instrumented caller.
	 * 
	 * @param request
	 * @param worker
	 * @param expectedMediaTypes
	 * @return Response
	 */
	protected Response handleAuditedServiceRequest(HttpServletRequest request, AbstractResourceDelegate<Object> worker,
			String... expectedMediaTypes) {
		// *****************************************************
		// Log boiler plate (hopefully to be added to filter soon).
		worker.id = getUserIdFromHeader(request);
		initLogContext(worker); // enable when fish tag is needed
		logger.info(getInitialAuditStatement(worker.getActionLabel()));
		Response response = null;
		try {
			// *****************************************************

			response = performServiceRequest(request, worker, expectedMediaTypes);

			// *****************************************************
			// Log boiler plate (hopefully to be added to filter soon).

		} catch (Throwable t) {
			if (t instanceof LiaisonAuditableRuntimeException) {
				logger.error(t); // Hack
				throw (LiaisonAuditableRuntimeException) t; // Hack
			} else {
				LiaisonAuditableRuntimeException e = new LiaisonUnhandledContainerException(t,
						Response.Status.INTERNAL_SERVER_ERROR); // Hack
				logger.error(e); // Hack
				throw e; // Hack
			}
		}
		exitLog(response);
		return response;
	}

	/**
	 * The KPI instrumented caller invokes the delegate worker.
	 * 
	 * @param request
	 * @param worker
	 * @param expectedMediaTypes
	 * @return Response
	 */
	public Response performServiceRequest(HttpServletRequest request, Callable<Object> worker,
			String... expectedMediaTypes) {
		beginMetricsCollection();

		Serializable serviceResponse = null;
		Response returnResponse = null;
		String serializationMediaType = null;
		boolean success = true;
		int httpCode = 200;
		try {
			serializationMediaType = determineDesiredSerialization(request);

			Object responseObject = worker.call();
			if (responseObject instanceof CommonResponseDTO) {
				CommonResponseDTO response = (CommonResponseDTO) responseObject;
				if (Messages.FAILURE.value().equalsIgnoreCase(response.getResponse().getStatus())) {
					httpCode = 400;
					success = false;
				}
			}
			if (responseObject instanceof Serializable) {
				// invoke the delegate to do the read work for
				// both get and list type operations
				serviceResponse = (Serializable) responseObject;

				// populate the response body
				returnResponse = marshalResponse(httpCode, serializationMediaType, serviceResponse);
			} else {
				// marshalling may have already been handled elsewhere
				returnResponse = (Response) responseObject;
			}

			// TODO - better exception handling
		} catch (RollbackException rbe) {
			success = false;
			// unwrap the real cause
			if (rbe.getCause() != null && rbe.getCause() instanceof PersistenceException) {
				throw (PersistenceException) rbe.getCause();
			} else {
				throw rbe;
			}
		} catch (RuntimeException ouc) {
			success = false;
			throw ouc;
		} catch (Exception onc) {
			success = false;
			throw new RuntimeException(onc);
		} finally {
			endMetricsCollection(success);

		}

		return returnResponse;
	}

	/**
	 * Implement initial audit statement common to exports service requests.
	 * 
	 * @param actionLabel
	 * @return
	 */
	protected abstract AuditStatement getInitialAuditStatement(String actionLabel);

	/**
	 * Implement Service specific metrics begin
	 */
	protected abstract void beginMetricsCollection();

	/**
	 * Implement Service specific metrics end
	 */
	protected abstract void endMetricsCollection(boolean success);

}
