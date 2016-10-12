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

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;
import com.liaison.mailbox.service.core.fsm.ProcessorExecutionStateDTO;

/**
 * The dao class for the PROCESSOR_EXECUTION_STATE database table.
 *
 * @author OFS
 */
public interface ProcessorExecutionStateDAO extends GenericDAO<ProcessorExecutionState> {

    String FIND_BY_PROCESSOR_ID = "ProcessorExecutionState.findByProcessorId";
    String FIND_NON_EXECUTING_PROCESSORS = "ProcessorExecutionState.findNonExecutingProcessors";
    String FIND_EXECUTING_PROCESSORS = "findExecutingProcessors";
    String FIND_EXECUTING_PROCESSORS_ALL = "findExecutingProcessorsAll";
    String FIND_EXECUTING_PROCESSOR_WITHIN_PERIOD = "ProcessorExecutionState.findExecutingProcessorsWithinPeriod";
    String PROCESSOR_ID = "processorId";
    String EXEC_STATUS = "exec_status";
    String INTERVAL_IN_HOURS = "interval";

    ProcessorExecutionState findByProcessorId(String processorId);

    void addProcessorExecutionState(ProcessorExecutionStateDTO executionStateDTO);

    List<String> findNonExecutingProcessors();

    List<ProcessorExecutionState> findExecutingProcessors(Map<String, Integer> pageOffsetDetails);

    int findAllExecutingProcessors();

}
