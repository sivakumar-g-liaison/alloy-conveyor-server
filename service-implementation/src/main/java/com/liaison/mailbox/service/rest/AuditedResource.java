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
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.exception.LiaisonUnhandledContainerException;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 * Resource class to audit all the incoming requests and their corresponding responses.
 * 
 * @author OFS
 */
public abstract class AuditedResource extends BaseResource {

	// *************************************************
	// *************************************************
	// ************ LOGGING STUFF **************
	private static final Logger logger = LogManager.getLogger(AuditedResource.class);
	public static final String HEADER_GUID = "guid";
	public static final String MULTIPLE = "MULTIPLE";
	
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
		//initLogContext(worker); // enable when fish tag is needed
		logger.info(getInitialAuditStatement(worker.getActionLabel()));
		Response response = null;
		try {
			// *****************************************************

			response = performServiceRequest(request, worker, expectedMediaTypes);
			initLogContext(worker);
			// *****************************************************
			// Log boiler plate (hopefully to be added to filter soon).

		} catch (Throwable t) {
		    logger.error(t); // Hack
			initLogContext(worker);
			if (t instanceof LiaisonAuditableRuntimeException) {
				throw (LiaisonAuditableRuntimeException) t; // Hack
			} else {
				LiaisonAuditableRuntimeException e = new LiaisonUnhandledContainerException(t,
						Response.Status.INTERNAL_SERVER_ERROR); // Hack
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
		}

		return returnResponse;
	}
	
	protected Response process(@Context HttpServletRequest request, AbstractResourceDelegate<Object> worker) {
		
		try {
			return handleAuditedServiceRequest(request, worker);
		} catch (LiaisonAuditableRuntimeException e) {
			if (!StringUtils.isEmpty(e.getResponseStatus().getStatusCode() + "")) {
				return marshalResponse(e.getResponseStatus().getStatusCode(), MediaType.TEXT_PLAIN, e.getMessage());
			}
			return marshalResponse(500, MediaType.TEXT_PLAIN, e.getMessage());
		}
	}

	/**
	 * Implement initial audit statement common to exports service requests.
	 * 
	 * @param actionLabel
	 * @return
	 */
	protected abstract AuditStatement getInitialAuditStatement(String actionLabel);

}
