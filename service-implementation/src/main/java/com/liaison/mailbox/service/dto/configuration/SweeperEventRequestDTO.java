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

import java.io.File;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("sweeperEventRequest")
public class SweeperEventRequestDTO {

    private File file;
    private boolean isSecuredPayload;
    private boolean isLensVisibility;
    private String globalProcessId;
    private String pipeLineID;
    private String contentType;
    private String mailBoxId;
    private String storageType;
    private Map<String, String> ttlMap;

    public SweeperEventRequestDTO() {
        super();
    }

    public SweeperEventRequestDTO(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
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

}
