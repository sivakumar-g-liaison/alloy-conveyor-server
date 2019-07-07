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

import com.wordnik.swagger.annotations.ApiModel;

/**
 * Data Transfer Object for datacenter details.
 *
 */
@ApiModel(value = "datacenter")
public class DatacenterDTO {
    
    private String name;
    private List<ProcessorDTO> processors;
    private int totalItems;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<ProcessorDTO> getProcessors() {
        return processors;
    }
    public void setProcessors(List<ProcessorDTO> processors) {
        this.processors = processors;
    }
    public int getTotalItems() {
        return totalItems;
    }
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}
