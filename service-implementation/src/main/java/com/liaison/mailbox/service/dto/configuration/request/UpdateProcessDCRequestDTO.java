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
 * DTO that contains the existing and new process_dc
 * 
 */
@JsonRootName("updateProcessDCRequest")
public class UpdateProcessDCRequestDTO {

    private String existingProcessDC;
    private String newProcessDC;

    public String getExistingProcessDC() {
        return existingProcessDC;
    }

    public void setExistingProcessDC(String existingProcessDC) {
        this.existingProcessDC = existingProcessDC;
    }

    public String getNewProcessDC() {
        return newProcessDC;
    }

    public void setNewProcessDC(String newProcessDC) {
        this.newProcessDC = newProcessDC;
    }

}
