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

import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.validation.PatternValidation;

/**
 * Data Transfer Object for the properties of FTP downloader.
 * 
 * @author OFS
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
public class FTPDownloaderPropertiesDTO extends StaticProcessorPropertiesDTO {

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
	private String fileTransferStatusIndicator;
	private boolean deleteFiles;
	private String includeFiles;
	private String excludeFiles;
	private boolean debugTranscript;
	private int scriptExecutionTimeout;
	private boolean includeSubDirectories;

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
	@PatternValidation(errorMessage = "Invalid Value for Retry interval", type = MailBoxConstants.PROPERTY_RETRY_INTERVAL)
	public int getRetryInterval() {
		return retryInterval;
	}
	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
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
	public void setIncludedFiles(String includeFiles) {
		this.includeFiles = includeFiles;
	}
	public String getExcludedFiles() {
		return excludeFiles;
	}
	public void setExcludedFiles(String excludeFiles) {
		this.excludeFiles = excludeFiles;
	}
	public boolean isDebugTranscript() {
		return debugTranscript;
	}
	public void setDebugTranscript(boolean debugTranscript) {
		this.debugTranscript = debugTranscript;
	}
	@PatternValidation(errorMessage = "Invalid value for script execution timeout", type = MailBoxConstants.PROPERTY_SCRIPT_EXECUTION_TIMEOUT)
	public int getScriptExecutionTimeout() {
		return scriptExecutionTimeout;
	}
	public void setScriptExecutionTimeout(int scriptExecutionTimeout) {
		this.scriptExecutionTimeout = scriptExecutionTimeout;
	}
	public boolean isIncludeSubDirectories() {
	    return includeSubDirectories;
	}
	public void setIncludeSubDirectories(boolean includeSubDirectories) {
	    this.includeSubDirectories = includeSubDirectories;
	}

}
