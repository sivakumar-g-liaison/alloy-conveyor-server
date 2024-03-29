/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.processor.properties;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.validation.PatternValidation;

/**
 * Data Transfer Object for the properties of HTTP uploader.
 *
 * @author OFS
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
public class HTTPUploaderPropertiesDTO extends StaticProcessorPropertiesDTO {

	private String httpVersion;
	private String httpVerb;
	private int retryAttempts;
	private int socketTimeout;
	private int connectionTimeout;
	private String url;
	private int port;
	private boolean chunkedEncoding;
	private String contentType;
	private String otherRequestHeader;
	@JsonIgnore
	private String errorFileLocation;
	@JsonIgnore
	private String processedFileLocation;
	private boolean recurseSubDirectories;
    private boolean directUpload;
    private int scriptExecutionTimeout;
    private int staleFileTTL;
    private boolean useFileSystem = false;
    private boolean saveResponsePayload;
    private String execution;
    private String category;

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
	@PatternValidation(errorMessage = "Invalid Value for Retry Attempts", type = MailBoxConstants.PROPERTY_RETRY_ATTEMPTS)
	public int getRetryAttempts() {
		return retryAttempts;
	}
	public void setRetryAttempts(int retryAttempts) {
		this.retryAttempts = retryAttempts;
	}
	@PatternValidation(errorMessage = "Invalid Value for Timeout", type = MailBoxConstants.PROPERTY_SOCKET_TIMEOUT)
	public int getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	@PatternValidation(errorMessage = "Invalid Value for Timeout", type = MailBoxConstants.PROPERTY_CONNECTION_TIMEOUT)
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
	public String getOtherRequestHeader() {
		return otherRequestHeader;
	}
	public void setOtherRequestHeader(String otherRequestHeader) {
		this.otherRequestHeader = otherRequestHeader;
	}
	public String getErrorFileLocation() {
		return errorFileLocation;
	}
	public void setErrorFileLocation(String errorFileLocation) {
		this.errorFileLocation = errorFileLocation;
	}
	public String getProcessedFileLocation() {
		return processedFileLocation;
	}
	public void setProcessedFileLocation(String processedFileLocation) {
		this.processedFileLocation = processedFileLocation;
	}
	public boolean isRecurseSubDirectories() {
		return recurseSubDirectories;
	}
	public void setRecurseSubDirectories(boolean recurseSubDirectories) {
		this.recurseSubDirectories = recurseSubDirectories;
	}
    public boolean isDirectUpload() {
        return directUpload;
    }
    public void setDirectUpload(boolean directUpload) {
        this.directUpload = directUpload;
    }
    @PatternValidation(errorMessage = "Invalid value for script execution timeout", type = MailBoxConstants.PROPERTY_SCRIPT_EXECUTION_TIMEOUT)
    public int getScriptExecutionTimeout() {
        return scriptExecutionTimeout;
    }
    public void setScriptExecutionTimeout(int scriptExecutionTimeout) {
        this.scriptExecutionTimeout = scriptExecutionTimeout;
    }
    @PatternValidation(errorMessage = "Invalid value for TTL", type = MailBoxConstants.PROPERTY_STALE_FILE_TTL)
    public int getStaleFileTTL() {
        return staleFileTTL;
    }
    public void setStaleFileTTL(int staleFileTTL) {
        this.staleFileTTL = staleFileTTL;
    }
    public boolean isUseFileSystem() {
        return useFileSystem;
    }
    public void setUseFileSystem(boolean useFileSystem) {
        this.useFileSystem = useFileSystem;
    }
    public boolean isSaveResponsePayload() {
        return saveResponsePayload;
    }
    public void setSaveResponsePayload(boolean saveResponsePayload) {
        this.saveResponsePayload = saveResponsePayload;
    }
    public String getExecution() {
        return execution;
    }
    public void setExecution(String execution) {
        this.execution = execution;
    }
    public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

}
