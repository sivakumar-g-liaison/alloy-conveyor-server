/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox;

import com.liaison.mailbox.rtdm.model.InboundFile;

/**
 * Data Transfer Object that contains the inbound file details.
 *
 * @author OFS
 */
public class InboundFileDTO {

    private String fileName;
    private String pguid;
    private String filePath;
    private String fileSize;
    private String processorId;
    private String inboundFileStatus;
    private String createdDate;
    private String modifiedDate;
    private String processDc;
    private String parentGlobalProcessId;
    private String createdBy;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getInboundFileStatus() {
        return inboundFileStatus;
    }

    public void setInboundFileStatus(String inboundFileStatus) {
        this.inboundFileStatus = inboundFileStatus;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getProcessDc() {
        return processDc;
    }

    public void setProcessDc(String processDc) {
        this.processDc = processDc;
    }

    public String getParentGlobalProcessId() {
        return parentGlobalProcessId;
    }

    public void setParentGlobalProcessId(String parentGlobalProcessId) {
        this.parentGlobalProcessId = parentGlobalProcessId;
    }

    public String getPguid() {
        return pguid;
    }

    public void setPguid(String pguid) {
        this.pguid = pguid;
    }

    /**
     * Copies the file details from file to dto.
     * 
     * @param file
     */
    public void copyFromEntity(InboundFile file) {

        if (null != file.getCreatedDate()) {
            this.setCreatedDate(file.getCreatedDate().toString());
        }

        this.setFileName(file.getFileName());
        this.setFilePath(file.getFilePath());
        this.setProcessorId(file.getProcessorId());
        this.setFileSize(file.getFileSize());
        this.setInboundFileStatus(file.getStatus());

        if (null != file.getModifiedDate()) {
            this.setModifiedDate(file.getModifiedDate().toString());
        }
        this.setPguid(file.getPguid());
        this.setParentGlobalProcessId(file.getParentGlobalProcessId());
        this.setProcessDc(file.getProcessDc());
        this.setCreatedBy(file.getCreatedBy());
    }
}