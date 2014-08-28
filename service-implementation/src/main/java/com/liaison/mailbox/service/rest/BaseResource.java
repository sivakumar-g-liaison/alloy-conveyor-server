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
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.util.StreamUtil;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.spectrum.client.model.KeyValuePair;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

/**
 * 
 * @author OFS
 * 
 */
public class BaseResource {

	private static final Logger logger = LogManager.getLogger(BaseResource.class);
	private static final Logger kpi = LogManager.getLogger("com.liaison.mailbox.metrics.KPI");

	protected void auditAttempt(String message) {

		AuditStatement auditStatement = new DefaultAuditStatement(Status.ATTEMPT,
				message,
				PCIV20Requirement.PCI10_2_5,
				PCIV20Requirement.PCI10_2_2);
		logger.info(auditStatement);

	}

	protected void auditSuccess(String message) {

		AuditStatement auditStatement = new DefaultAuditStatement(Status.SUCCEED, message + "- operation is success");
		logger.info(auditStatement);

	}

	protected void auditFailure(String message) {

		AuditStatement auditStatement = new DefaultAuditStatement(Status.FAILED, message + "- operation failed");
		logger.info(auditStatement);

	}

	/**
	 * Audits it based on the response status
	 * 
	 * @param response
	 * @param operationName
	 */
	public void doAudit(ResponseDTO response, String operationName) {

		if (isSuccess(response)) {
			auditSuccess(operationName);
		} else {
			auditFailure(operationName);
		}
	}

	/**
	 * Checks the response status
	 * 
	 * @param serviceResponse
	 * @return true if it is success, false otherwise
	 */
	public boolean isSuccess(ResponseDTO serviceResponse) {

		if (Messages.SUCCESS.value().equals(serviceResponse.getStatus())) {
			return true;
		}

		return false;
	}

	/**
	 * Call this when entering a service, fish tags log for this thread.
	 * 
	 * @param <T>
	 * 
	 */
	protected <T> void initLogContext(AbstractResourceDelegate<T> worker) {

		ThreadContext.put("@Path", worker.getFishTagPath()); // audit log context
		ThreadContext.put("@Service", worker.getFishTagService()); // audit log context
		if (null != worker.getID()) {
			ThreadContext.put("@Id", worker.getID()); // audit log context
		}
		if (null != worker.getQueryParams()) {
			for (Entry<String, String> paramEntry : worker.getQueryParams().entrySet()) {
				ThreadContext.put("@Param_" + paramEntry.getKey(), paramEntry.getValue());
			}
		}
	}

	/**
	 * success audit statement.
	 */
	protected AuditStatement successExitStatement = new DefaultAuditStatement(Status.SUCCEED,
			"response is success (2xx)");
	protected AuditStatement failExitStatement = new DefaultAuditStatement(Status.FAILED,
			"response is not success (not 2xx), assume fail");
	protected AuditStatement unknownExitStatusStatement = new DefaultAuditStatement(Status.SUCCEED,
			"response status not set, assuming success");

	/**
	 * Call this when concluding a service, indicates successful exit (or fail) based on response status.
	 * 
	 * Also cleans up thread context
	 * 
	 * @param response
	 */
	protected void exitLog(Response response) {
		if (response.getStatus() >= 200 && response.getStatus() <= 299) {
			logger.info(successExitStatement);
		} else if (response.getStatus() == -1) {
			logger.info(unknownExitStatusStatement);
		} else {
			logger.info(failExitStatement);
		}
	}

	/**
	 * Is the a query string param of xml? Otherwise it will be JSON serialization.
	 * 
	 * @param request
	 * @return a content-type string such as "application/json"
	 */
	public String determineDesiredSerialization(HttpServletRequest request) {
		String serializationMediaType;
		boolean isSerializeToJson = true;

		Enumeration<String> params = request.getParameterNames();

		while (params.hasMoreElements()) {
			String name = params.nextElement();
			if ("xml".equals(name.toLowerCase())) {
				isSerializeToJson = false;
				break;
			}
		}

		serializationMediaType = isSerializeToJson ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML;
		return serializationMediaType;
	}

	/**
	 * 
	 * @param httpCode
	 * @param contentType
	 * @param serviceResponse
	 * @param headers
	 * @return
	 */
	public Response marshalResponse(int httpCode, String contentType, Object serviceResponse, KeyValuePair... headers) {
		String responseBody;

		ResponseBuilder response = new ResponseBuilderImpl();
		try {

			if (null != serviceResponse) {
				if (contentType.equals(MediaType.APPLICATION_JSON)) {
					responseBody = MailBoxUtil.marshalToJSON(serviceResponse);
					response = Response.status(httpCode).header("Content-Type", MediaType.APPLICATION_JSON)
							.entity(responseBody);
				} else {
					response = Response.status(httpCode).header("Content-Type", contentType).entity(serviceResponse);
				}
			} else {
				response = Response.status(httpCode).header("Content-Type", contentType);
			}
		} catch (IOException | JAXBException e) {
			response = Response.status(500).header("Content-Type", MediaType.TEXT_PLAIN)
					.entity("Response serialization failure.");
			logger.error(e.getMessage(), e);
		}

		for (KeyValuePair header : headers) {
			response = response.header(header.getKey(), header.getValue());
		}

		// TODO remove this
		response.header("Access-Control-Allow-Origin", "*");

		return response.build();
	}

	protected void logKPIMetric(Object message, String markerLabel) {
		Marker marker = MarkerManager.getMarker(markerLabel);
		kpi.info(marker, message);
	}

	/**
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public String getRequestBody(HttpServletRequest request) throws UnsupportedEncodingException, IOException {
		if (null == request) {
			return "";
		} else {
			String requestString = new String(StreamUtil.streamToBytes(request.getInputStream()), "UTF-8").trim();

			return requestString;
		}
	}
}
