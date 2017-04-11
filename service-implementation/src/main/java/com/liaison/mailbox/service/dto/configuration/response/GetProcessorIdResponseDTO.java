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

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 * Class used to retrieve the processor ID details
 */
public class GetProcessorIdResponseDTO extends CommonResponseDTO {

    private List<String> processorGuids;
    private String processorName;

    public List<String> getProcessorGuids() {
        return processorGuids;
    }
    public void setProcessorGuids(List<String> processorGuids) {
        this.processorGuids = processorGuids;
    }
    public String getProcessorName() {
        return processorName;
    }
    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }
}
