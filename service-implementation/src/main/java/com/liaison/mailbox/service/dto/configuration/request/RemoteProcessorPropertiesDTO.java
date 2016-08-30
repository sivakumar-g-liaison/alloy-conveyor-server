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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.validation.PatternValidation;

/**
 * Data Transfer Object that contains the properties of remote processors.
 * 
 * @author OFS
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
	private List<HTTPOtherRequestHeaderDTO> otherRequestHeader;
	private boolean securedPayload;
	private boolean handOverExecutionToJavaScript;
	private boolean deleteFiles;
	@JsonIgnore
	private boolean deleteFileAfterSweep;
	
	private String fileTransferStatusIndicator;
    private String includeFiles;
    private String excludeFiles;
    private String numOfFilesThreshold;
    private String payloadSizeThreshold;
    @JsonIgnore
    private String errorFileLocation;
    @JsonIgnore
    private String processedFileLocation;
    @JsonIgnore
    private String fileRenameFormat;
    @JsonIgnore
    private String sweepedFileLocation;
    private boolean debugTranscript;
    private boolean lensVisibility;
    private boolean httpListenerAuthCheckRequired;
    private boolean createFoldersInRemote;
    
    private boolean sweepSubDirectories;
    private boolean recurseSubDirectories;
    private boolean directUpload;
    private int scriptExecutionTimeout;
    private int syncAndAsyncConnectionTimeout;
    
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

	@PatternValidation(errorMessage = "Invalid URL.", type = MailBoxConstants.PROPERTY_URL)
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

	public List<HTTPOtherRequestHeaderDTO> getOtherRequestHeader() {
		return otherRequestHeader;
	}

	public void setOtherRequestHeader(List<HTTPOtherRequestHeaderDTO> otherRequestHeader) {
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
	
	public boolean getDeleteFiles() {
		return deleteFiles;
	}
	public void setDeleteFiles(boolean deleteFiles) {
		this.deleteFiles = deleteFiles;
	}

	public boolean isSecuredPayload() {
		return securedPayload;
	}

	public void setSecuredPayload(boolean securedPayload) {
		this.securedPayload = securedPayload;
	}

    public String getFileTransferStatusIndicator() {
        return fileTransferStatusIndicator;
    }

    public void setFileTransferStatusIndicator(String fileTransferStatusIndicator) {
        this.fileTransferStatusIndicator = fileTransferStatusIndicator;
    }

    public String getIncludeFiles() {
        return includeFiles;
    }

    public void setIncludeFiles(String includeFiles) {
        this.includeFiles = includeFiles;
    }

    public String getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    @PatternValidation(errorMessage = "Invalid Value for Number of Files Threshold", type = MailBoxConstants.PROPERTY_NO_OF_FILES_THRESHOLD)
    public String getNumOfFilesThreshold() {
        return numOfFilesThreshold;
    }

    public void setNumOfFilesThreshold(String numOfFilesThreshold) {
        this.numOfFilesThreshold = numOfFilesThreshold;
    }

    public String getPayloadSizeThreshold() {
        return payloadSizeThreshold;
    }

    public void setPayloadSizeThreshold(String payloadSizeThreshold) {
        this.payloadSizeThreshold = payloadSizeThreshold;
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

    public String getFileRenameFormat() {
        return fileRenameFormat;
    }

    public void setFileRenameFormat(String fileRenameFormat) {
        this.fileRenameFormat = fileRenameFormat;
    }

    public String getSweepedFileLocation() {
        return sweepedFileLocation;
    }

    public void setSweepedFileLocation(String sweepedFileLocation) {
        this.sweepedFileLocation = sweepedFileLocation;
    }

    public boolean isDebugTranscript() {
        return debugTranscript;
    }

    public void setDebugTranscript(boolean debugTranscript) {
        this.debugTranscript = debugTranscript;
    }

    public boolean isLensVisibility() {
        return lensVisibility;
    }

    public void setLensVisibility(boolean lensVisibility) {
        this.lensVisibility = lensVisibility;
    }

    public boolean isHttpListenerAuthCheckRequired() {
        return httpListenerAuthCheckRequired;
    }

    public void setHttpListenerAuthCheckRequired(
            boolean httpListenerAuthCheckRequired) {
        this.httpListenerAuthCheckRequired = httpListenerAuthCheckRequired;
    }

    public boolean isCreateFoldersInRemote() {
        return createFoldersInRemote;
    }

    public void setCreateFoldersInRemote(boolean createFoldersInRemote) {
        this.createFoldersInRemote = createFoldersInRemote;
    }

    public boolean isSweepSubDirectories() {
        return sweepSubDirectories;
    }

    public void setSweepSubDirectories(boolean sweepSubDirectories) {
        this.sweepSubDirectories = sweepSubDirectories;
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

    public int getScriptExecutionTimeout() {
        return scriptExecutionTimeout;
    }

    public void setScriptExecutionTimeout(int scriptExecutionTimeout) {
        this.scriptExecutionTimeout = scriptExecutionTimeout;
    }
    
    public int getSyncAndAsyncConnectionTimeout() {
        return syncAndAsyncConnectionTimeout;
    }

    public void setSyncAndAsyncConnectionTimeout(int syncAndAsyncConnectionTimeout) {
        this.syncAndAsyncConnectionTimeout = syncAndAsyncConnectionTimeout;
    }
    
}
