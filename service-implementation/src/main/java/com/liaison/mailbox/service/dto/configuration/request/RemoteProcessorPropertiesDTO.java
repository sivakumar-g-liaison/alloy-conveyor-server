/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.configuration.request;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * @author OFS
 * 
 */
@JsonRootName("remoteProcessorProperties")
public class RemoteProcessorPropertiesDTO {

	private String httpVersion;
	private String httpVerb;
	private int retryAttempts;
	private int socketTimeout;
	private int connectionTimeout;
	private String url;
	private int port;
	private boolean chunkedEncoding;
	private String contentType;
	private String encodingFormat;
	private boolean passive;
	private boolean binary;
	private int retryInterval;
	private String pipeLineID;
	private String httpListenerPipeLineId;
	private List<HttpOtherRequestHeaderDTO> otherRequestHeader;
	private boolean securedPayload;
	private boolean handOverExecutionToJavaScript;
	private boolean deleteFileAfterSweep;

	public boolean isDeleteFileAfterSweep() {
		return deleteFileAfterSweep;
	}

	public void setDeleteFileAfterSweep(boolean deleteFileAfterSweep) {
		this.deleteFileAfterSweep = deleteFileAfterSweep;
	}

	public boolean isHandOverExecutionToJavaScript() {
		return handOverExecutionToJavaScript;
	}

	public void setHandOverExecutionToJavaScript(
			boolean handOverExecutionToJavaScript) {
		this.handOverExecutionToJavaScript = handOverExecutionToJavaScript;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	public String getHttpVerb() {
		return httpVerb;
	}

	public void setHttpVerb(String httpVerb) {
		this.httpVerb = httpVerb;
	}

	public int getRetryAttempts() {
		return retryAttempts;
	}

	public void setRetryAttempts(int retryAttempts) {
		this.retryAttempts = retryAttempts;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isChunkedEncoding() {
		return chunkedEncoding;
	}

	public void setChunkedEncoding(boolean chunkedEncoding) {
		this.chunkedEncoding = chunkedEncoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getEncodingFormat() {
		return encodingFormat;
	}

	public void setEncodingFormat(String encodingFormat) {
		this.encodingFormat = encodingFormat;
	}

	public List<HttpOtherRequestHeaderDTO> getOtherRequestHeader() {
		return otherRequestHeader;
	}

	public void setOtherRequestHeader(List<HttpOtherRequestHeaderDTO> otherRequestHeader) {
		this.otherRequestHeader = otherRequestHeader;
	}

	public boolean isPassive() {
		return passive;
	}

	public void setPassive(boolean passive) {
		this.passive = passive;
	}

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public String getPipeLineID() {
		return pipeLineID;
	}

	public void setPipeLineID(String pipeLineID) {
		this.pipeLineID = pipeLineID;
	}

	public String getHttpListenerPipeLineId() {
		return httpListenerPipeLineId;
	}

	public void setHttpListenerPipeLineId(String httpListenerPipeLineId) {
		this.httpListenerPipeLineId = httpListenerPipeLineId;
	}

	public boolean isSecuredPayload() {
		return securedPayload;
	}

	public void setSecuredPayload(boolean securedPayload) {
		this.securedPayload = securedPayload;
	}	
}
