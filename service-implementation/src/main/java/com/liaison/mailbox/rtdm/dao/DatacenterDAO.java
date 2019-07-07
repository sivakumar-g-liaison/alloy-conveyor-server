/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import java.util.Map;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.Datacenter;

/**
 * DAO class for Datacenter.
 *
 */
public interface DatacenterDAO extends GenericDAO<Datacenter> {

    public static final String PROCESING_DC = "processing_dc";
    public static final String NAME = "name";
    String FIND_PROCESSING_DC_BY_NAME = "Datacenter.findProcessingDatacenterByName";

    String UPDATE_PROCESSING_DATACENTRE = "UPDATE DATACENTER SET PROCESSING_DC =:" + PROCESING_DC +
            " WHERE NAME =:" + NAME;

    /**
     * API to update the processing datacenter
     * 
     * @param datacenterMap
     */
    void updateDatacenter(Map<String, String> datacenterMap);

    /**
     * API to create the processing datacenter
     * 
     * @param datacenterMap
     */
    void createDatacenter(Map<String, String> datacenterMap);

    /**
     * API to find the processing datacenter
     * 
     * @param name
     * @return processingDc
     */
    String findProcessingDatacenterByName(String name);

}
