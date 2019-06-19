/**
 * Copyright 2019 Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;

import org.codehaus.jackson.map.annotate.JsonRootName;

import java.util.Map;

@JsonRootName("sweeperEventRequest")
public class SweeperEventRequestDTO {

    private String fileName;
    private String filePath;
    private String modifiedTime;
    private long size=-1;
    private boolean isSecuredPayload;
    private boolean isLensVisibility;
    private String globalProcessId;
    private String pipeLineID;
    private String contentType;
    private String mailBoxId;
    private String processorId;
    private String storageType;
    private Map<String, String> ttlMap;
    private int retryCount;

    public SweeperEventRequestDTO() {
        super();
    }

    public String getMailBoxId() {
        return mailBoxId;
    }

    public void setMailBoxId(String mailBoxId) {
        this.mailBoxId = mailBoxId;
    }

    public String getStorageType() {
	    return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public Map<String, String> getTtlMap() {
        return ttlMap;
    }

    public void setTtlMap(Map<String, String> ttlMap) {
        this.ttlMap = ttlMap;
    }

    public boolean isSecuredPayload() {
        return isSecuredPayload;
    }

    public void setSecuredPayload(boolean isSecuredPayload) {
        this.isSecuredPayload = isSecuredPayload;
    }

    public boolean isLensVisibility() {
        return isLensVisibility;
    }

    public void setLensVisibility(boolean isLensVisibility) {
        this.isLensVisibility = isLensVisibility;
    }

    public String getGlobalProcessId() {
        return globalProcessId;
    }

    public void setGlobalProcessId(String globalProcessId) {
        this.globalProcessId = globalProcessId;
    }

    public String getPipeLineID() {
        return pipeLineID;
    }

    public void setPipeLineID(String pipeLineID) {
        this.pipeLineID = pipeLineID;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }
}
