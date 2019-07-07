/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;

import java.util.List;

/**
 * DTO for file ingress processors.
 */
public class FilesIngressProcessorDTO {

    private List<IngressFilesDTO> files;
    private String processorName;
    private String processorGuid;
    private String status;
    private String payloadUri;

    public List<IngressFilesDTO> getFiles() {
        return files;
    }

    public void setFiles(List<IngressFilesDTO> files) {
        this.files = files;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getProcessorGuid() {
        return processorGuid;
    }

    public void setProcessorGuid(String processorGuid) {
        this.processorGuid = processorGuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayloadUri() {
        return payloadUri;
    }

    public void setPayloadUri(String payloadUri) {
        this.payloadUri = payloadUri;
    }
}
