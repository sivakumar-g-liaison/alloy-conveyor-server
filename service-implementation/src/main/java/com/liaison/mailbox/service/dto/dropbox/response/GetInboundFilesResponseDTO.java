/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox.response;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.InboundFileDTO;

/**
 * Data Transfer Object used for retrieving the inbound files.
 *
 */
@JsonRootName("getInboundFilesResponse")
public class GetInboundFilesResponseDTO extends CommonResponseDTO {

    private static final long serialVersionUID = 1L;
    private List<InboundFileDTO> inboundFiles;
    private int totalItems;

    public List<InboundFileDTO> getInboundFiles() {
        return inboundFiles;
    }

    public void setInboundFiles(List<InboundFileDTO> inboundFiles) {
        this.inboundFiles = inboundFiles;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}
