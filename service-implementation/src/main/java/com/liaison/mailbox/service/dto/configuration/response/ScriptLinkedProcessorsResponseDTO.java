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
import com.liaison.mailbox.service.dto.configuration.PropertiesFileDTO;

/**
 * Data Transfer Object used for sending scriptURI service responses.
 *
 */
@JsonRootName("getScriptProcessorListResponse")
public class ScriptLinkedProcessorsResponseDTO extends CommonResponseDTO {

    private static final long serialVersionUID = 1L;
    private List<ProcessorLinkedScriptDTO> processors;
    private int totalItems;

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<ProcessorLinkedScriptDTO> getProcessors() {
        return processors;
    }

    public void setProcessors(List<ProcessorLinkedScriptDTO> processors) {
        this.processors = processors;
    }
}