/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.RuntimeProcessors;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;

/**
 * The dao class for the PROCESSORS database table.
 *
 */
public interface RuntimeProcessorsDAO extends GenericDAO<RuntimeProcessors> {

    String FIND_BY_PROCESSOR_ID = "RuntimeProcessors.findByProcessorId";
    String PROCESSOR_ID = "processorId";

    RuntimeProcessors findByProcessorId(String processorId);

    void addProcessor(ProcessorExecutionStateDTO executionStateDTO);
}
