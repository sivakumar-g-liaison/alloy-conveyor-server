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
 * Data Transfer Object for the properties for sweeper.
 * 
 * @author OFS
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
public class SweeperPropertiesDTO extends StaticProcessorPropertiesDTO {

	private String pipeLineID;
	private boolean securedPayload;
	@JsonIgnore
	private boolean deleteFileAfterSweep;
	@JsonIgnore
	private String fileRenameFormat;
	private String numOfFilesThreshold;
	private String payloadSizeThreshold;
	@JsonIgnore
	private String sweepedFileLocation;
	private String includeFiles;
	private String excludeFiles;
	private boolean lensVisibility;
	private boolean sweepSubDirectories;
	private String contentType;
	private String sort;
    private int staleFileTTL;
	
	public String getPipeLineID() {
		return pipeLineID;
	}
	public void setPipeLineID(String pipeLineID) {
		this.pipeLineID = pipeLineID;
	}
	public boolean isSecuredPayload() {
		return securedPayload;
	}
	public void setSecuredPayload(boolean securedPayload) {
		this.securedPayload = securedPayload;
	}
	public boolean isDeleteFileAfterSweep() {
		return deleteFileAfterSweep;
	}
	public void setDeleteFileAfterSweep(boolean deleteFileAfterSweep) {
		this.deleteFileAfterSweep = deleteFileAfterSweep;
	}
	public String getFileRenameFormat() {
		return fileRenameFormat;
	}
	public void setFileRenameFormat(String fileRenameFormat) {
		this.fileRenameFormat = fileRenameFormat;
	}
	@PatternValidation(errorMessage = "Invalid Value for Number of Files Threshold.", type = MailBoxConstants.PROPERTY_NO_OF_FILES_THRESHOLD)
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
	public String getSweepedFileLocation() {
		return sweepedFileLocation;
	}
	public void setSweepedFileLocation(String sweepedFileLocation) {
		this.sweepedFileLocation = sweepedFileLocation;
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
	public boolean isLensVisibility() {
		return lensVisibility;
	}
	public void setLensVisibility(boolean lensVisibility) {
		this.lensVisibility = lensVisibility;
	}
	public boolean isSweepSubDirectories() {
        return sweepSubDirectories;
    }
    public void setSweepSubDirectories(boolean sweepSubDirectories) {
        this.sweepSubDirectories = sweepSubDirectories;
    }
    public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
    public String getSort() {
        return sort;
    }
    public void setSort(String sort) {
        this.sort = sort;
    }
    @PatternValidation(errorMessage = "Invalid value for TTL", type = MailBoxConstants.PROPERTY_STALE_FILE_TTL)
    public int getStaleFileTTL() {
        return staleFileTTL;
    }
    public void setStaleFileTTL(int staleFileTTL) {
        this.staleFileTTL = staleFileTTL;
    }

}
