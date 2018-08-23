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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    String FIND_BY_PROCESSOR_ID_AND_NOT_PROCESSING = "ProcessorExecutionState.findIdleProcessorByProcessorId";
    String FIND_PROCESSORS = "ProcessorExecutionState.findProcessors";
    String FIND_NON_EXECUTING_PROCESSORS = "ProcessorExecutionState.findNonExecutingProcessors";
    String FIND_EXECUTING_PROCESSORS = "findExecutingProcessors";
    String FIND_EXECUTING_PROCESSORS_ALL = "findExecutingProcessorsAll";
    String FIND_EXECUTING_PROCESSOR_WITHIN_PERIOD = "ProcessorExecutionState.findExecutingProcessorsWithinPeriod";
    String FIND_EXECUTING_PROCESSOR_WITH_TRIGGERED_PERIOD = "ProcessorExecutionState.findExecutingProcessorsWithTriggeredPeriod";
    String PROCESSOR_ID = "processorId";
    String PGUID = "pguid";
    String EXEC_STATUS = "exec_status";
    String NEW_EXEC_STATUS = "new_exec_status";
    String INTERVAL_IN_HOURS = "interval";
    String NODE = "node";
    String THREAD_NAME = "threadName";
    String LAST_EXECUTION_STATE = "lastExecutionState";
    String LAST_EXECUTION_DATE = "lastExecutionDate";
    String NODE_IN_USE = "nodeInUse";
    String MODIFIED_DATE = "modifiedDate";
    String ORIGINATING_DC = "originating_dc";

    String GET_PROCESSOR_EXECUTION_STATE_FOR_UPDATE = "SELECT PGUID, EXEC_STATUS FROM PROCESSOR_EXEC_STATE STATE"
            + " WHERE STATE.PROCESSOR_ID =:" + PROCESSOR_ID
            + " AND STATE.EXEC_STATUS <>:" + EXEC_STATUS
            + " FOR UPDATE";

    String GET_PROCESSOR_EXECUTION_STATE = "SELECT PGUID, EXEC_STATUS FROM PROCESSOR_EXEC_STATE STATE"
            + " WHERE STATE.PROCESSOR_ID =:" + PROCESSOR_ID
            + " FOR UPDATE";

    String UPDATE_PROCESSOR_EXECUTION_STATE_VALUES = "UPDATE PROCESSOR_EXEC_STATE" +
            " SET EXEC_STATUS =:" + EXEC_STATUS +
            " , LAST_EXEC_STATE =:" + LAST_EXECUTION_STATE +
            " , LAST_EXEC_DATE = SYSTIMESTAMP" +
            " , THREAD_NAME =:" + THREAD_NAME +
            " , NODE_IN_USE =:" + NODE_IN_USE +
            " , MODIFIED_DATE =:" + MODIFIED_DATE +
            " WHERE PGUID =:" + PGUID;

    String UPDATE_PROCESSOR_EXECUTION_STATE = "UPDATE PROCESSOR_EXEC_STATE" +
            " SET EXEC_STATUS =:" + EXEC_STATUS +
            " WHERE PGUID =:" + PGUID;
    
    String UPDATE_PROCESSOR_EXECUTION_STATE_ON_INIT = "UPDATE PROCESSOR_EXEC_STATE" +
            " SET EXEC_STATUS =:" + NEW_EXEC_STATUS +
            " WHERE NODE_IN_USE =:" + NODE_IN_USE +
            " AND EXEC_STATUS =:" + EXEC_STATUS;
    
    String UPDATE_PROCESSOR_EXECUTION_STATE_STATUS_BY_PROCESSORID = "UPDATE PROCESSOR_EXEC_STATE" +
            " SET EXEC_STATUS =:" + EXEC_STATUS +
            " WHERE PROCESSOR_ID =:" + PROCESSOR_ID;

    ProcessorExecutionState findByProcessorId(String processorId);

    /**
     * finds processor execution state by processor guid and updates the status to PROCESSING if it is not already running
     * <p>
     * It is using 'PESSIMISTIC_WRITE' to lock the processor entity during update
     *
     * @param processorId the processor guid
     * @return processor execution state pguid and status
     */
    Object[] findByProcessorIdAndUpdateStatus(String processorId);

    /**
     * Updates processor execution state details
     *
     * @param pguid ProcessorExecutionState guid
     * @param status status to be updated
     */
    void updateProcessorExecutionState(String pguid, String status);
    
    /**
     * Updates processor execution state for the current node on server start.
     * Upldate all "PROCESSING" state processors to "FAILED"
     * 
     * @param node
     */
    void updateStuckProcessorsExecutionState(String node);

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

    List<ProcessorExecutionState> findExecutingProcessors();
    
    /**
     * Method to find the executing processor for the period of time.
     * 
     * @param timeUnit
     * @param value
     * @return list of processors in "PROCESSING" state
     */
    List<ProcessorExecutionState> findExecutingProcessors(TimeUnit timeUnit, int value);

    int findAllExecutingProcessors();
    
    /**
     * Update the ProcessorExecutionState Status by processorId
     * 
     * @param processorId
     * @param status
     */
    void updateProcessorExecutionStateStatusByProcessorId( String processorId,  String status);

}
