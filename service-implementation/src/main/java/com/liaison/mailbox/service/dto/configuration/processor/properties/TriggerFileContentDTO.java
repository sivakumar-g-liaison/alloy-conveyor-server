/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.configuration.processor.properties;

import java.io.Serializable;
import java.util.Map;

/**
 * Trigger file content DTO
 */
public class TriggerFileContentDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String parentGlobalProcessId;

    private Map<String, String> filePathStatusIndex;

    public String getParentGlobalProcessId() {
        return parentGlobalProcessId;
    }

    public void setParentGlobalProcessId(String parentGlobalProcessId) {
        this.parentGlobalProcessId = parentGlobalProcessId;
    }

    public Map<String, String> getFilePathStatusIndex() {
        return filePathStatusIndex;
    }

    public void setFilePathStatusIndex(Map<String, String> filePathStatusIndex) {
        this.filePathStatusIndex = filePathStatusIndex;
    }
}
