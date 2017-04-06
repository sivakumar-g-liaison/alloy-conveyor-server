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

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * DTO that contains the processorIds required to update the execution Status to failed.
 * 
 */
@JsonRootName("updateProcessorsExecutionStateRequest")
public class UpdateProcessorsExecutionStateRequestDTO {
    
    @JsonProperty("guids")
    private List<String> guids;
    
    public List<String> getGuids() {
        return guids;
    }
    
    public void setGuids(List<String> guids) {
        this.guids = guids;
    }

}
