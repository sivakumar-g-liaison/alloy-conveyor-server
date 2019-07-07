/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorLinkedScriptDTO;
import com.liaison.mailbox.service.dto.configuration.ProcessorScriptDTO;

/**
 * Data Transfer Object used for sending script service responses.
 *
 */
@JsonRootName("getScriptListResponse")
public class ProcessorScriptResponseDTO extends CommonResponseDTO {

    private static final long serialVersionUID = 1L;

    private List<ProcessorScriptDTO> scripts;
    private int totalItems;

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<ProcessorScriptDTO> getScripts() {
        return scripts;
    }

    public void setScripts(List<ProcessorScriptDTO> scripts) {
        this.scripts = scripts;
    }
}
