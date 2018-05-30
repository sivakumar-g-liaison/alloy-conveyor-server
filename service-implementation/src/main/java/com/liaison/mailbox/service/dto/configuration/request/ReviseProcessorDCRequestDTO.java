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

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * Data Transfer Object that contains the fields required for processor dc revision.
 */

@JsonRootName("reviseProcessorDcRequest")
public class ReviseProcessorDCRequestDTO {

    private String processorGuid;
    private String processorDC;
    private String processorType;

    public String getProcessorGuid() {
        return processorGuid;
    }

    public void setProcessorGuid(String processorGuid) {
        this.processorGuid = processorGuid;
    }

    public String getProcessorDC() {
        return processorDC;
    }

    public void setProcessorDC(String processorDC) {
        this.processorDC = processorDC;
    }

    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }
}
