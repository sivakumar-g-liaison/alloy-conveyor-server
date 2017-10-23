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

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.logging.LogTags;
import com.liaison.commons.util.StreamUtil;
import com.liaison.commons.util.StringUtil;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.gem.util.GEMConstants;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.spectrum.client.model.KeyValuePair;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for all resources.
 *
 * @author OFS
 */
public class BaseResource {

	public static final String CONTENT_TYPE = "Content-Type";
	private static final Logger logger = LogManager.getLogger(BaseResource.class);
	public static final String HEADER_USER_ID = "UserId";
    public static final String MAILBOX_ID = "mailboxId";
    public static final String MAILBOX_NAME = "mailboxName";
    public static final String TYPE = "type";
    public static final String TRIM_RESPONSE = "trimResponse";
    public static final String HEADER_GUID = "guid";
    public static final String SIID = "siid";
    public static final String MULTIPLE = "MULTIPLE";
    private static final int MAX_STATUS_CODE = 299;
	protected static final String HARD_DELETE = "hardDelete";

    protected void auditAttempt(String message) {

		AuditStatement auditStatement = new DefaultAuditStatement(Status.ATTEMPT, message, PCIV20Requirement.PCI10_2_5,
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
		return Messages.SUCCESS.value().equals(serviceResponse.getStatus());
	}

	/**
	 * Call this when entering a service, fish tags log for this thread.
	 *
	 * @param <T>
	 *
	 */
	protected <T> void initLogContext(AbstractResourceDelegate<T> worker) {

		ThreadContext.put(LogTags.RELATIVE_PATH, worker.getFishTagPath()); // audit log context
		ThreadContext.put(LogTags.SERVICE, worker.getFishTagService()); // audit log context
		if (null != worker.getID()) {
			ThreadContext.put(LogTags.USER_PRINCIPAL, worker.getID()); // audit log context
			ThreadContext.put(LogTags.USER_PRINCIPAL_ID, worker.getID()); // audit log context

		}
		if (null != worker.getQueryParams()) {
			ThreadContext.put(LogTags.PGUIDS, worker.getFishTagService() + ":" + worker.getQueryParams().get("guid"));
			for (Entry<String, String> paramEntry : worker.getQueryParams().entrySet()) {
				ThreadContext.put(LogTags.QUERY_PARAM + paramEntry.getKey(), paramEntry.getValue());
			}
		}
	}

	/**
	 * Call this when entering a runtime service, fish tags log for this thread.
	 *
	 * @param path The rest resource path
	 * @param className
	 * @param methodName
	 * @param globalProcessId
	 * @param pipelineId
	 */
	protected void initLogContext(String path, String className, String methodName, String globalProcessId, String pipelineId) {

		ThreadContext.put(LogTags.RELATIVE_PATH, path); // audit log context
		ThreadContext.put(LogTags.SERVICE, className + ": " + methodName); // audit log context
		ThreadContext.put(LogTags.GLOBAL_PROCESS_ID, globalProcessId); // audit log context
		if (pipelineId != null && !pipelineId.isEmpty()) {
			ThreadContext.put(LogTags.PIPELINE_ID, pipelineId); // audit log context
		}
	}

	/**
	 * success audit statement.
	 */
	protected AuditStatement successExitStatement = new DefaultAuditStatement(Status.SUCCEED, "Success (2xx)");
	protected AuditStatement failExitStatement = new DefaultAuditStatement(Status.FAILED, "Failure");
	protected AuditStatement unknownExitStatusStatement = new DefaultAuditStatement(Status.SUCCEED, "assume Success");

	/**
	 * Call this when concluding a service, indicates successful exit (or fail) based on response status.
	 *
	 * Also cleans up thread context
	 *
	 * @param response
	 */
	protected void exitLog(Response response) {
		if (response.getStatus() >= Response.Status.OK.getStatusCode() && response.getStatus() <= MAX_STATUS_CODE) {
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
     * @param httpCode
     * @param contentType
     * @param serviceResponse
     * @param headers
     * @return
     */
    public Response marshalResponse(int httpCode, String contentType, Object serviceResponse, KeyValuePair... headers) {
        String responseBody;

        ResponseBuilder response = null;
        try {

            if (null != serviceResponse) {

                if (contentType.equals(MediaType.APPLICATION_JSON)) {
                    responseBody = MailBoxUtil.marshalToJSON(serviceResponse);
                    response = Response.status(httpCode)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .entity(responseBody);
                } else {
                    response = Response.status(httpCode)
                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                            .entity(serviceResponse);
                }
            } else {
                response = Response.status(httpCode).header(HttpHeaders.CONTENT_TYPE, contentType);
            }
        } catch (IOException e) {
            response = Response.status(500)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                    .entity("Response serialization failure.");
            logger.error(e.getMessage(), e);
        }

        for (KeyValuePair header : headers) {
            response = response.header(header.getKey(), header.getValue());
        }

        return response.build();
    }

	/**
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public String getRequestBody(HttpServletRequest request)
			throws UnsupportedEncodingException, IOException {
		if (null == request) {
			return "";
		} else {
			String requestString = new String(StreamUtil.streamToBytes(request.getInputStream()), "UTF-8").trim();

			return requestString;
		}
	}

	protected String getUserIdFromHeader(final HttpServletRequest request) {
		String userId = request.getHeader(HEADER_USER_ID);
		if (userId == null || userId.isEmpty()) {
			return "unknown-user";
		}

		return userId;
	}

	/**
	 * Returns header map from http request.
	 *
	 * @param request The HTTPRequest
	 * @return Headers
	 */
	protected Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {

		Map<String, List<String>> headers = new HashMap<>();

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {

			String headerName = headerNames.nextElement();
			if (!HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(headerName)
					&& !HttpHeaders.AUTHORIZATION.equalsIgnoreCase(headerName)) {
				List<String> headerValues = new ArrayList<>();
				Enumeration<String> values = request.getHeaders(headerName);

				while (values.hasMoreElements()) {
					headerValues.add(values.nextElement());
				}

				headers.put(headerName, headerValues);
			}
		}

		return headers;
	}

	/**
	 * Get workticket properties from the http request
	 *
	 * @param request HTTP request
	 * @return Map contains workticket properties
	 */
    protected Map<String, Object> getRequestProperties(HttpServletRequest request, String globalProcessId) {

		Map<String, Object> headers = new HashMap<>();
		headers.put(MailBoxConstants.HTTP_METHOD, request.getMethod());
		headers.put(MailBoxConstants.HTTP_QUERY_STRING, request.getQueryString());
		headers.put(MailBoxConstants.HTTP_REMOTE_PORT, request.getRemotePort());
		headers.put(MailBoxConstants.HTTP_CHARACTER_ENCODING,
				(request.getCharacterEncoding() != null ? request.getCharacterEncoding() : ""));
		headers.put(MailBoxConstants.HTTP_REMOTE_USER,
				(request.getRemoteUser() != null ? request.getRemoteUser() : "unknown-user"));
		headers.put(MailBoxConstants.HTTP_REMOTE_ADDRESS, request.getRemoteAddr());
		headers.put(MailBoxConstants.HTTP_REQUEST_PATH, request.getRequestURL().toString());
		headers.put(MailBoxConstants.HTTP_CONTENT_TYPE,
				(request.getContentType() != null ? request.getContentType() : ContentType.TEXT_PLAIN.getMimeType()));

        String gpidFromHeader = request.getHeader(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER);
        if (MailBoxUtil.isEmpty(gpidFromHeader)) {
            headers.put(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER, globalProcessId);
        } else {
            headers.put(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER, gpidFromHeader);
        }

		return headers;
	}
	
	/**
	 * Method to get all request headers and put it in a Map
	 * 
	 * @param request
	 * @return Map containing header Names and values as key value pair
	 */
	protected Map<String, String> getRequestHeaderValues(HttpServletRequest request) {
		
		Map<String, String> headers = new HashMap<>();
		for (String headerName : Collections.list(request.getHeaderNames())) {
			headers.put(headerName, request.getHeader(headerName));
		}
		return headers;
	}

	/**
	 * Constructs the response for all the dropbox use case
	 * 
	 * @param loginId the login id of the user
	 * @param authenticationToken GUM authentication token
	 * @param manifestResponse GEM Manifest response
	 * @param responseBody The response body
	 * @return ResponseBuilder
	 */
	protected ResponseBuilder constructResponse(String loginId,
			String authenticationToken,
			GEMManifestResponse manifestResponse,
			String responseBody) {

		// response message construction
		ResponseBuilder builder = Response.ok()
				.header(MailBoxConstants.ACL_MANIFEST_HEADER, manifestResponse.getManifest())
				.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, manifestResponse.getSignature())
				.header(MailBoxConstants.DROPBOX_AUTH_TOKEN, authenticationToken)
				.header(MailBoxConstants.DROPBOX_LOGIN_ID, loginId)
				.type(MediaType.APPLICATION_JSON)
				.entity(responseBody).status(Response.Status.OK);

		// set signer public key/key-group guid in response header based on response from gem
		builder = MailBoxUtil.isEmpty(manifestResponse.getPublicKeyGroupGuid())
					? builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID, manifestResponse.getPublicKeyGuid())
					: builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GROUP_GUID, manifestResponse.getPublicKeyGroupGuid());
		return builder;

	}

	/**
	 * Constructs the response for all the dropbox use case
	 * 
	 * @param loginId the login id of the user
	 * @param authenticationToken GUM authentication token
	 * @param manifestResponse GEM Manifest response
	 * @param responseBody The response body
	 * @return ResponseBuilder
	 */
	protected ResponseBuilder constructResponse(String loginId,
			String authenticationToken,
			GEMManifestResponse manifestResponse,
			InputStream payload) {

		// response message construction
		ResponseBuilder builder = Response.ok()
				.header(MailBoxConstants.ACL_MANIFEST_HEADER, manifestResponse.getManifest())
				.header(MailBoxConstants.ACL_SIGNED_MANIFEST_HEADER, manifestResponse.getSignature())
				.header(MailBoxConstants.DROPBOX_AUTH_TOKEN, authenticationToken)
				.header(MailBoxConstants.DROPBOX_LOGIN_ID, loginId)
				.type(MediaType.APPLICATION_JSON)
				.entity(payload).status(Response.Status.OK);

		// set signer public key/key-group guid in response header based on response from gem
		builder = MailBoxUtil.isEmpty(manifestResponse.getPublicKeyGroupGuid())
				? builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GUID, manifestResponse.getPublicKeyGuid())
				: builder.header(GEMConstants.HEADER_KEY_ACL_SIGNATURE_PUBLIC_KEY_GROUP_GUID, manifestResponse.getPublicKeyGroupGuid());

		return builder;

	}

	/**
	 * Mandatory Validation for the dropbox header fields
	 * @param loginId
	 * @param authenticationToken
	 * @param aclManifest
	 */
	protected void dropboxMandatoryValidation(String loginId, String authenticationToken, String aclManifest) {

		if (StringUtil.isNullOrEmptyAfterTrim(authenticationToken)
				|| StringUtil.isNullOrEmptyAfterTrim(aclManifest)
				|| StringUtil.isNullOrEmptyAfterTrim(loginId)) {
			throw new MailBoxConfigurationServicesException(Messages.REQUEST_HEADER_PROPERTIES_MISSING, Response.Status.BAD_REQUEST);
		}

	}

    /**
     * gets remote address from the http request
     *
     * @param request http request
     * @return ip address
     */
    protected String getRemoteAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader(com.google.common.net.HttpHeaders.X_FORWARDED_FOR);
        if (null == ipAddress) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
