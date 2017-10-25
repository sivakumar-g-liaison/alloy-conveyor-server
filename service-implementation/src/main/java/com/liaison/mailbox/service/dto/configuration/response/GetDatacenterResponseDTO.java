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
import com.liaison.mailbox.service.dto.configuration.DatacenterDTO;

/**
 * Data Transfer Object used for retrieving the datacenter.
 *
 */
@JsonRootName("getDatacenterResponse")
public class GetDatacenterResponseDTO extends CommonResponseDTO {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private List<DatacenterDTO> datacenetrs;

    public List<DatacenterDTO> getDatacenetrs() {
        return datacenetrs;
    }

    public void setDatacenetrs(List<DatacenterDTO> datacenetrs) {
        this.datacenetrs = datacenetrs;
    }
	
}
