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
 * Data Transfer Object for the properties of FTP uploader.
 * 
 * @author OFS
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
public class FTPUploaderPropertiesDTO extends StaticProcessorPropertiesDTO {

	private String url;
	private int port;
	private int retryAttempts;
	private int socketTimeout;
	private int connectionTimeout;
	private boolean chunkedEncoding;
	private String contentType;
	private boolean passive;
	private boolean binary;
	private int retryInterval;
	@JsonIgnore
	private String errorFileLocation;
	@JsonIgnore
	private String processedFileLocation;
	private String fileTransferStatusIndicator;
	@JsonIgnore
	private boolean deleteFiles;
	private String includeFiles;
	private String excludeFiles;
	private boolean debugTranscript;
	private boolean createFoldersInRemote;
	private boolean recurseSubDirectories;
    private boolean directUpload;	
    private int scriptExecutionTimeout;

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
	public String getFileTransferStatusIndicator() {
		return fileTransferStatusIndicator;
	}
	public void setFileTransferStatusIndicator(String fileTransferStatusIndicator) {
		this.fileTransferStatusIndicator = fileTransferStatusIndicator;
	}
	public boolean getDeleteFiles() {
		return deleteFiles;
	}
	public void setDeleteFiles(boolean deleteFiles) {
		this.deleteFiles = deleteFiles;
	}
	public String getIncludedFiles() {
		return includeFiles;
	}
	public void setIncludedFiles(String includedFiles) {
		this.includeFiles = includedFiles;
	}
	public String getExcludedFiles() {
		return excludeFiles;
	}
	public void setExcludedFiles(String excludedFiles) {
		this.excludeFiles = excludedFiles;
	}
	public boolean isDebugTranscript() {
		return debugTranscript;
	}
	public void setDebugTranscript(boolean debugTranscript) {
		this.debugTranscript = debugTranscript;
	}
	public boolean isCreateFoldersInRemote() {
        return createFoldersInRemote;
    }
    public void setCreateFoldersInRemote(boolean createFoldersInRemote) {
        this.createFoldersInRemote = createFoldersInRemote;
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

    @PatternValidation(errorMessage = "Invalid Value for Script Execution Timeout", type = MailBoxConstants.PROPERTY_SCRIPT_EXECUTION_TIMEOUT)
    public int getScriptExecutionTimeout() {
        return scriptExecutionTimeout;
    }
    public void setScriptExecutionTimeout(int scriptExecutionTimeout) {
        this.scriptExecutionTimeout = scriptExecutionTimeout;
    }
}
