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
import com.liaison.fsm.FSMDao;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.model.FSMState;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;

import java.sql.Timestamp;
import java.util.List;

/**
 * The dao class for the FSM_STATE database table.
 * 
 * @author OFS
 */
public interface FSMStateDAO extends GenericDAO<FSMState>, FSMDao<ProcessorStateDTO, ExecutionEvents> {

	String FIND_FSM_STATE_BY_NAME = "FSMState.findByExecutionId";
	String EXECUTION_ID = "execution_id";
	String FIND_ALL_EXECUTING_PROC = "FSMState.findAllExecutingProcessors";
	String FIND_EXECUTING_PROC_BY_VALUE = "FSMState.findProcessorsByValue";
	String FIND_EXECUTING_PROC_BY_DATE = "FSMState.findProcessorsByDate";
	String FIND_EXECUTING_PROC_BY_VALUE_AND_DATE = "FSMState.findProcessorsByValueAndDate";
	String FIND_ALL_EXECUTING_PROC_BY_PROCESSORID = "FSMState.findAllExecutingProcessorsByProcessorId";
	String FIND_MOST_RECENT_SUCCESSFUL_EXECUTION_OF_PROCESSOR = "FSMState.findMostRecentSuccessfulExecutionOfProcessor";
	String FIND_NON_SLA_VERIFIED_FSM_EVENTS_BY_VALUE = "FSMState.findNonSLAVerifiedFSMEventsByValue";
	String INTERVAL_IN_HOURS = "interval_in_hours";
	String BY_VALUE = "by_value";
	String FROM_DATE = "from_date";
	String TO_DATE = "to_date";
	String PROCESSOR_ID = "processor_id";
	String FSM_STATE_VALUE = "fsm_state_value";
	String SLA_VERIFICATION_STATUS = "sla_verification_status";

	/**
	 * Find FSMState by given status value.
	 *
	 * @param state
	 * @return FSMState
	 */
	FSMState find(String state);

	/**
	 * Find list of FSMStateValue by given time.
	 *
	 * @param listJobsIntervalInHours
	 * @return The list of FSMStateValue
	 */
	List<FSMStateValue> findAllExecutingProcessors(Timestamp listJobsIntervalInHours);

	/**
	 * Find list of FSMStateValue by given status and time.
	 *
	 * @param value
	 * @param listJobsIntervalInHours
	 * @return The list of FSMStateValue
	 */
	List<FSMStateValue> findExecutingProcessorsByValue(String value, Timestamp listJobsIntervalInHours);

	/**
	 * Find list of FSMStateValue by given date.
	 *
	 * @param frmDate
	 * @param toDate
	 * @return The list of FSMStateValue
	 */
	List<FSMStateValue> findExecutingProcessorsByDate(String frmDate, String toDate);

	/**
	 * Find list of FSMStateValue by given status and date.
	 *
	 * @param status
	 * @param frmDate
	 * @param toDate
	 * @return The list of FSMStateValue
	 */
	List<FSMStateValue> findExecutingProcessorsByValueAndDate(String value, String frmDate, String toDate);

	/**
	 * Find list of FSMStateValue by given processorId, status, time Interval
	 *
	 * @param processorId
	 * @param fsmStateValue
	 * @param timeInterval
	 *
	 * @return The list of FSMStateValue
	 */
	List<FSMStateValue> findExecutingProcessorsByProcessorId(String processorId, Timestamp timeInterval);


	/**
	 * Find the most recent successful execution of a processor based on given processor Id
	 *
	 * @param processorId
	 * @return FSMStateValue
	 */

	List<FSMStateValue> findMostRecentSuccessfulExecutionOfProcessor(String processorId, ProcessorType processorType);


	/**
	 * Find the non sla verified events by given fsm state value
	 *
	 * @param processorId
	 * @return list of FSMState
	 */
	List <FSMState> findNonSLAVerifiedFSMEventsByValue(String processorId, Timestamp processorLastExecution, String value);

	/**
	 * Find the non sla verified events by given fsm state value
	 *
	 * @param processorId
	 * @return list of FSMState
	 */
	List<FSMState> findNonSLAVerifiedFileStagedEvents(String processorId, Timestamp processorLastExecution, ProcessorType processorType);

}
