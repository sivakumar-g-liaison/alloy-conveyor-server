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
 * Data Transfer Object for the properties for conditional sweeper.
 * 
 * @author OFS
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "staticProperties")
public class ConditionalSweeperPropertiesDTO extends StaticProcessorPropertiesDTO {

    private boolean securedPayload;
    private boolean lensVisibility;
    private boolean allowEmptyFiles = true;
    private boolean deleteEmptyDirectoryAfterSwept = true;
    private String pipeLineID;
    private String numOfFilesThreshold;
    private String payloadSizeThreshold;
    private String triggerFile;
    private String contentType;
    private String sort;
    private int staleFileTTL;
    private boolean sweepSubDirectories;

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

    @PatternValidation(errorMessage = "Invalid trigger file", type = MailBoxConstants.TRIGGER_FILE)
    public String getTriggerFile() {
        return triggerFile;
    }

    public void setTriggerFile(String triggerFile) {
        this.triggerFile = triggerFile;
    }

    public boolean isLensVisibility() {
        return lensVisibility;
    }

    public void setLensVisibility(boolean lensVisibility) {
        this.lensVisibility = lensVisibility;
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

    public boolean isAllowEmptyFiles() {
        return allowEmptyFiles;
    }

    public void setAllowEmptyFiles(boolean allowEmptyFiles) {
        this.allowEmptyFiles = allowEmptyFiles;
    }

    public boolean isDeleteEmptyDirectoryAfterSwept() {
        return deleteEmptyDirectoryAfterSwept;
    }

    public void setDeleteEmptyDirectoryAfterSwept(boolean deleteEmptyDirectoryAfterSwept) {
        this.deleteEmptyDirectoryAfterSwept = deleteEmptyDirectoryAfterSwept;
    }

    public boolean isSweepSubDirectories() {
        return sweepSubDirectories;
    }

    public void setSweepSubDirectories(boolean sweepSubDirectories) {
        this.sweepSubDirectories = sweepSubDirectories;
    }
}
