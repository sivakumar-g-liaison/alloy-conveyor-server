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

/**
 * Data Transfer Object for the properties for sweeper.
 * 
 * @author OFS
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
public class SweeperPropertiesDTO extends StaticProcessorPropertiesDTO {

	private String pipeLineID;
	private boolean securedPayload;
	private boolean deleteFileAfterSweep;
	private String fileRenameFormat;
	private String numOfFilesThreshold;
	private String payloadSizeThreshold;
	private String sweepedFileLocation;
	private String includeFiles;
	private String excludeFiles;
	private boolean lensVisibility;



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
}
