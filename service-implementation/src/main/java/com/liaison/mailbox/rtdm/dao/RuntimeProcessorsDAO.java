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
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.model.RuntimeProcessors;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;

import java.util.List;

/**
 * The dao class for the PROCESSORS database table.
 *
 */
public interface RuntimeProcessorsDAO extends GenericDAO<RuntimeProcessors> {

    String FIND_BY_PROCESSOR_ID = "RuntimeProcessors.findByProcessorId";
    String PROCESSOR_ID = "processorId";

    String FIND_NON_RUNNING_PROCESSORS = "SELECT P.PROCESSOR_ID FROM PROCESSORS P"
            + " LEFT JOIN PROCESSOR_EXEC_STATE STATE ON P.PROCESSOR_ID = STATE.PROCESSOR_ID"
            + " WHERE P.PROCESSOR_ID IN (:" + PROCESSOR_ID + ")"
            + " AND P.CLUSTER_TYPE =:" + MailBoxConstants.CLUSTER_TYPE
            + " AND (STATE.PGUID IS NULL OR STATE.EXEC_STATUS != 'PROCESSING')";

    RuntimeProcessors findByProcessorId(String processorId);

    /**
     * find non running processors
     * @param processors processors matches the profile
     * @return list of processors that not running
     */
    List<String> findNonRunningProcessors(List<String> processors);

    void addProcessor(ProcessorExecutionStateDTO executionStateDTO);
}
