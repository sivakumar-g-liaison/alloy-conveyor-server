/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpRequest;

/**
 * 
 * @author OFS
 * 
 */
public class SessionContext {
	protected static final String PIPELINE_ID_TOKEN = "PipelineId";
	protected static final String DOCUMENT_PROTOCOL_TOKEN = "DocumentProtocol";
	protected static final String DOCUMENT_TYPE_TOKEN = "DocumentType";

	protected static final String GATEWAY_HEADER_PREFIX = "X-GATE-";
	protected static final String GATEWAY_REMOTE_ADDRESS_HEADER = GATEWAY_HEADER_PREFIX + "RemoteAddress";
	protected static final String GATEWAY_REMOTE_PORT_HEADER = GATEWAY_HEADER_PREFIX + "RemotePort";
	protected static final String GATEWAY_REMOTE_USER_HEADER = GATEWAY_HEADER_PREFIX + "RemoteUser";
	protected static final String GATEWAY_GLOBAL_PROCESS_ID_HEADER = GATEWAY_HEADER_PREFIX + "GlobalProcessId";
	protected static final String GATEWAY_REQUEST_TIMESTAMP_HEADER = GATEWAY_HEADER_PREFIX + "RequestTimestamp";
	protected static final String GATEWAY_REQUEST_PATH_HEADER = GATEWAY_HEADER_PREFIX + "RequestPath";
	protected static final String GATEWAY_REQUEST_METHOD_HEADER = GATEWAY_HEADER_PREFIX + "RequestMethod";
	protected static final String GATEWAY_QUERY_STRING_HEADER = GATEWAY_HEADER_PREFIX + "QueryString";
	protected static final String GATEWAY_CONTENT_TYPE_HEADER = GATEWAY_HEADER_PREFIX + "ContentType";
	protected static final String GATEWAY_CHARACTER_ENCODING_HEADER = GATEWAY_HEADER_PREFIX + "CharacterEncoding";
	protected static final String GATEWAY_PIPELINE_ID_HEADER = GATEWAY_HEADER_PREFIX + "PipelineId";
	protected static final String GATEWAY_DOCUMENT_PROTOCOL_HEADER = GATEWAY_HEADER_PREFIX + "DocumentProtocol";
	protected static final String GATEWAY_DOCUMENT_TYPE_HEADER = GATEWAY_HEADER_PREFIX + "DocumentType";
	protected static final String GATEWAY_ID = GATEWAY_HEADER_PREFIX + "mailboxid";

	protected String _requestorIPAddress = null;
	protected int _requestorPort = 0;
	protected String _remoteUser = null;
	protected String _globalProcessId = null;
	protected Date _requestTimestamp = null;
	protected String _requestPath = null;
	protected String _requestMethod = null;
	protected String _queryString = null;
	protected String _contentType = null;
	protected String _characterEncoding = null;
	protected String _pipelineId = null;
	protected String _documentProtocol = null;
	protected String _documentType = null;
	protected String _payloadUri = null;
	protected int _contentLength = 0;
	protected String _mailboxId = null;


	protected ArrayList<Header> _headers = null;


	public SessionContext() {
		_headers = new ArrayList<Header>();
	}

	/**
	 * Copies all the data from the HttpServletRequest to SessionContext.
	 * 
	 * @param request HttpServletRequest
	 */
	public void copyFrom(HttpServletRequest request) {
		setRemoteAddress(request.getRemoteAddr());
		setRemotePort(request.getRemotePort());
		setRemoteUser(request.getRemoteUser());
		setRequestPath(request.getRequestURL().toString());
		setMethod(request.getMethod());
		setQueryString(request.getQueryString());
		setCharacterEncoding(request.getCharacterEncoding());
		setContentType(request.getContentType());
		setContentLength(request.getContentLength());

		copyRequestHeadersToSessionContext(request);
		extractPipelineIdTokens(request);
	}

	/**
	 * Copies all the request header from HttpServletRequest to SessionContext.
	 * 
	 * @param request HttpServletRequest
	 */
	protected void copyRequestHeadersToSessionContext(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();

			addHeader(headerName, request.getHeaders(headerName));
		}
	}

	protected void extractPipelineIdTokens(HttpServletRequest request) {
		String[] tokens = getTokens(request);

		for (String token : tokens) {
			String[] parts = token.split("=");

			if ((parts != null) && (parts.length == 2))

			{

				if (parts[0].equalsIgnoreCase(DOCUMENT_PROTOCOL_TOKEN)) {
					setDocumentProtocol(parts[1]);
				} else if (parts[0].equalsIgnoreCase(DOCUMENT_TYPE_TOKEN)) {
					setDocumentType(parts[1]);
				}
				// No else clause, ignore extra pieces.
			}
			// No else clause, ignoring potential extra stuff in the path.
		}
	}

	protected String[] getTokens(HttpServletRequest request) {
		String queryString = request.getQueryString();
		String pathInfo = request.getPathInfo();

		if ((queryString == null) && (pathInfo == null)) {
			// TODO - this should be an error.
			return null;
		}

		String[] tokens = null;

		if (pathInfo != null) {
			tokens = pathInfo.split("/");
		} else {
			tokens = queryString.split("&");
		}

		return tokens;
	}

	/**
	 * Copies all the data from SessionContext to HttpRequest.
	 * 
	 * @param request HttpRequest
	 */
	public void copyTo(HttpRequest request) {
		addNonNullHeaders(request, GATEWAY_REMOTE_ADDRESS_HEADER, getRemoteAddress());
		addNonNullHeaders(request, GATEWAY_REMOTE_PORT_HEADER, getRemotePort() + "");
		addNonNullHeaders(request, GATEWAY_REMOTE_USER_HEADER, getRemoteUser());
		addNonNullHeaders(request, GATEWAY_GLOBAL_PROCESS_ID_HEADER, getGlobalProcessId());
		addNonNullHeaders(request, GATEWAY_REQUEST_TIMESTAMP_HEADER, getRequestTimestamp() + "");
		addNonNullHeaders(request, GATEWAY_REQUEST_PATH_HEADER, getRequestPath());
		addNonNullHeaders(request, GATEWAY_REQUEST_METHOD_HEADER, getMethod());
		addNonNullHeaders(request, GATEWAY_QUERY_STRING_HEADER, getQueryString());
		addNonNullHeaders(request, GATEWAY_CONTENT_TYPE_HEADER, getContentType());
		addNonNullHeaders(request, GATEWAY_CHARACTER_ENCODING_HEADER, getCharacterEncoding());
		addNonNullHeaders(request, GATEWAY_PIPELINE_ID_HEADER, getPipelineId());
		addNonNullHeaders(request, GATEWAY_DOCUMENT_PROTOCOL_HEADER, getDocumentProtocol());
		addNonNullHeaders(request, GATEWAY_DOCUMENT_TYPE_HEADER, getDocumentType());
		addNonNullHeaders(request, GATEWAY_ID, getMailboxId());

		copyHeadersToOutgoingRequest(request);
	}

	protected void addNonNullHeaders(HttpRequest request, String headerName, String headerValue) {
		if (headerValue != null) {
			request.addHeader(headerName, headerValue);
		}
	}

	protected void copyHeadersToOutgoingRequest(HttpRequest request) {
		for (Header header : _headers) {
			request.addHeader(GATEWAY_HEADER_PREFIX + header.name, header.value);
		}
	}


	public void setRemoteAddress(String requestorIPAddress) {
		_requestorIPAddress = requestorIPAddress;
	}

	public String getRemoteAddress() {
		return _requestorIPAddress;
	}

	public void setRemotePort(int requestorPort) {
		_requestorPort = requestorPort;
	}

	public int getRemotePort() {
		return _requestorPort;
	}

	public void setRemoteUser(String remoteUser) {
		_remoteUser = remoteUser;
	}

	public String getRemoteUser() {
		return _remoteUser;
	}

	public void setGlobalProcessId(String globalProcessId) {
		_globalProcessId = globalProcessId;
	}

	public String getGlobalProcessId() {
		return _globalProcessId;
	}

	public void setRequestTimestamp(Date timestamp) {
		_requestTimestamp = timestamp;
	}

	public Date getRequestTimestamp() {
		return _requestTimestamp;
	}

	public void setRequestPath(String path) {
		_requestPath = path;
	}

	public String getRequestPath() {
		return _requestPath;
	}

	public void setMethod(String method) {
		_requestMethod = method;
	}

	public String getMethod() {
		return _requestMethod;
	}

	public void setQueryString(String queryString) {
		_queryString = queryString;
	}

	public String getQueryString() {
		return _queryString;
	}

	public void setContentType(String contentType) {
		_contentType = contentType;
	}

	public String getContentType() {
		return _contentType;
	}

	public void setContentLength(int contentLength) {
		_contentLength = contentLength;
	}

	public int getContentLength() {
		return _contentLength;
	}

	public void setCharacterEncoding(String characterEncoding) {
		_characterEncoding = characterEncoding;
	}

	public String getCharacterEncoding() {
		return _characterEncoding;
	}

	public void setPipelineId(String pipelineId) {
		_pipelineId = pipelineId;
	}

	public String getPipelineId() {
		return _pipelineId;
	}

	public void setDocumentType(String documentType) {
		_documentType = documentType;
	}

	public String getDocumentType() {
		return _documentType;
	}

	public void setDocumentProtocol(String documentProtocol) {
		_documentProtocol = documentProtocol;
	}

	public String getDocumentProtocol() {
		return _documentProtocol;
	}

	public void setPayloadUri(String payloadUri) {
		_payloadUri = payloadUri;
	}

	public String getPayloadUri() {
		return _payloadUri;
	}

	public String getMailboxId() {
		return _mailboxId;
	}

	public void setMailboxId(String _mailboxId) {
		this._mailboxId = _mailboxId;
	}


	public void addHeader(String name, Enumeration<String> values) {
		while (values.hasMoreElements()) {
			String value = values.nextElement();

			Header header = new Header();
			header.name = name;
			header.value = value;

			_headers.add(header);
		}
	}

	protected class Header {
		public String name = null;
		public String value = null;
	}
}
