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
import java.util.Set;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;

@JsonRootName("sweeperEventRequest")
public class SweeperEventRequestDTO {

    private File file;
    private boolean isSecuredPayload;
    private boolean isLensVisibility;
    private String globalProcessId;
    private String pipeLineID;
    private String ContentType;
    private String mailBoxId;
    private Map<String, String> ttlMap;
    private Set<ProcessorProperty> dynamicProperties;

    @SuppressWarnings("unused")
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

    public Map<String, String> getTtlMap() {
        return ttlMap;
    }

    public void setTtlMap(Map<String, String> ttlMap) {
        this.ttlMap = ttlMap;
    }

    public Set<ProcessorProperty> getDynamicProperties() {
        return dynamicProperties;
    }

    public void setDynamicProperties(Set<ProcessorProperty> dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
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
        return ContentType;
    }

    public void setContentType(String contentType) {
        ContentType = contentType;
    }

}
