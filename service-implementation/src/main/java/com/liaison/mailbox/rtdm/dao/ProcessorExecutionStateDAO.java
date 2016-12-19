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

import javax.persistence.LockModeType;

/**
 * The dao class for the PROCESSOR_EXECUTION_STATE database table.
 *
 * @author OFS
 */
public interface ProcessorExecutionStateDAO extends GenericDAO<ProcessorExecutionState> {

    String FIND_BY_PROCESSOR_ID = "ProcessorExecutionState.findByProcessorId";
    String FIND_BY_PROCESSOR_ID_AND_NOT_PROCESSING = "ProcessorExecutionState.findIdleProcessorByProcessorId";
    String FIND_PROCESSORS = "ProcessorExecutionState.findProcessors";
    String FIND_NON_EXECUTING_PROCESSORS = "ProcessorExecutionState.findNonExecutingProcessors";
    String FIND_EXECUTING_PROCESSORS = "findExecutingProcessors";
    String FIND_EXECUTING_PROCESSORS_ALL = "findExecutingProcessorsAll";
    String FIND_EXECUTING_PROCESSOR_WITHIN_PERIOD = "ProcessorExecutionState.findExecutingProcessorsWithinPeriod";
    String PROCESSOR_ID = "processorId";
    String EXEC_STATUS = "exec_status";
    String INTERVAL_IN_HOURS = "interval";
    String NODE = "node";
    String THREAD_NAME = "threadName";

    ProcessorExecutionState findByProcessorId(String processorId);

    /**
     * finds processor execution state by processor guid and updates the status to PROCESSING if it is not already running
     * <p>
     * It is using 'PESSIMISTIC_WRITE' to lock the processor entity during update
     *
     * @param processorId the processor guid
     * @return processor execution state
     */
    ProcessorExecutionState findByProcessorIdAndUpdateStatus(String processorId);

    /**
     * lists the processors in descending order
     *
     * @param node filters by node in use
     * @param threadName filters by thread name
     * @return list of processor execution state
     */
    List<ProcessorExecutionState> findProcessors(String node, String threadName);

    void addProcessorExecutionState(ProcessorExecutionStateDTO executionStateDTO);

    List<String> findNonExecutingProcessors();

    List<ProcessorExecutionState> findExecutingProcessors(Map<String, Integer> pageOffsetDetails);

    int findAllExecutingProcessors();

}
