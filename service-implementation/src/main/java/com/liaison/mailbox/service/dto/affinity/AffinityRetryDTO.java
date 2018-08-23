/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.affinity;

import java.util.List;
import java.util.Map;

public class AffinityRetryDTO {

    private String clusterType;
    private Map<String, String> datacenterMap;
    private List<String> processorGuids;

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public Map<String, String> getDatacenterMap() {
        return datacenterMap;
    }

    public void setDatacenterMap(Map<String, String> datacenterMap) {
        this.datacenterMap = datacenterMap;
    }

    public List<String> getProcessorGuids() {
        return processorGuids;
    }

    public void setProcessorGuids(List<String> processorGuids) {
        this.processorGuids = processorGuids;
    }
}
