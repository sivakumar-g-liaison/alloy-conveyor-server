/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.jpa.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.fsm.FSMDao;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.FSMState;
import com.liaison.mailbox.jpa.model.FSMStateValue;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;

/**
 * @author OFS
 * 
 */
@NamedQueries({ @NamedQuery(name = FSMStateDAO.FIND_FSM_STATE_BY_NAME, 
		query = "SELECT state FROM FSMState state WHERE state.executionId = :" + FSMStateDAO.EXECUTION_ID),
@NamedQuery(name = FSMStateDAO.FIND_ALL_PROC_EXECUTING,
		query = "select stateVal from FSMStateValue stateVal"
				+ " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
				+ " inner join sta.executionState staVal"
				+ " where sta.processorId = : " + FSMStateDAO.PROCESSOR_ID + "and staVal.createdDate >= :" + FSMStateDAO.INTERVAL_IN_HOURS + " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_PROC_EXECUTING_BY_VALUE,
		query = "select stateVal from FSMStateValue stateVal"
				+ " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
				+ " inner join sta.executionState staVal"
				+ " where staVal.createdDate >= :" + FSMStateDAO.INTERVAL_IN_HOURS + " and stateVal.value = :" + FSMStateDAO.BY_VALUE
				+ " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_PROC_EXECUTING_BY_DATE,
        query = "select stateVal from FSMStateValue stateVal"
                + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
                + " inner join sta.executionState staVal"
                + " where stateVal.createdDate >= :" + FSMStateDAO.FROM_DATE + " and stateVal.createdDate <= :" + FSMStateDAO.TO_DATE
                + " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_PROC_EXECUTING_BY_VALUE_AND_DATE,
        query = "select stateVal from FSMStateValue stateVal"
                + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
                + " inner join sta.executionState staVal"
                + " where stateVal.createdDate >= :" + FSMStateDAO.FROM_DATE + " and stateVal.createdDate <= :" + FSMStateDAO.TO_DATE
                + " and stateVal.value = :" + FSMStateDAO.BY_VALUE
                + " group by staVal.fsmState)"),
@NamedQuery(name = FSMStateDAO.FIND_ALL_PROC_EXECUTING_BY_PROCESSORID,
        query = "select stateVal from FSMStateValue stateVal"
                + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
                + " inner join sta.executionState staVal"
                + " where sta.processorId = :" + FSMStateDAO.PROCESSOR_ID 
                + " and staVal.createdDate >= :" + FSMStateDAO.INTERVAL_IN_HOURS + ")"),
@NamedQuery(name = FSMStateDAO.FIND_MOST_RECENT_SUCCESSFUL_EXECUTION_OF_PROCESSOR,
        query = "select stateVal from FSMStateValue stateVal"
                + " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
                + " inner join sta.executionState staVal"
                + " where sta.processorId = :" + FSMStateDAO.PROCESSOR_ID 
                + " and staVal.value = :" + FSMStateDAO.BY_VALUE + ")"),
/*@NamedQuery(name = FSMStateDAO.FIND_NON_SLA_VERIFIED_FILE_STAGED_EVENTS, 
		query = "select state from FSMState state" 
				+ " where state.slaVerificationStatus = :" + FSMStateDAO.SLA_VERIFICATION_STATUS 
				+ " and state.processorId = :" + FSMStateDAO.PROCESSOR_ID
				+ " and state.executionState IN (select stateVal from FSMStateValue stateVal where stateVal.createdDate < :" +FSMStateDAO.TO_DATE 
				+ " and stateVal.value = :" + FSMStateDAO.BY_VALUE + ")")*/
@NamedQuery(name = FSMStateDAO.FIND_NON_SLA_VERIFIED_FSM_EVENTS_BY_VALUE, 
		query = "select state from FSMState state" 
				+ " inner join state.executionState stateValue"
				+ " where state.slaVerificationStatus = :" + FSMStateDAO.SLA_VERIFICATION_STATUS 
				+ " and state.processorId = :" + FSMStateDAO.PROCESSOR_ID
				+ " and stateValue.createdDate <= :" +FSMStateDAO.TO_DATE 
				+ " and stateValue.value = :" + FSMStateDAO.BY_VALUE),
@NamedQuery(name = FSMStateDAO.FIND_NON_SLA_VERIFIED_FILE_STAGED_EVENTS, 
		query = "select state from FSMState state" 
				+ " inner join state.executionState stateValue"
				+ " where state.slaVerificationStatus = :" + FSMStateDAO.SLA_VERIFICATION_STATUS 
				+ " and state.processorId = :" + FSMStateDAO.PROCESSOR_ID
				+ " and stateValue.createdDate < :" +FSMStateDAO.TO_DATE 
				+ " and stateValue.value = :" + FSMStateDAO.BY_VALUE)
                
})

public interface FSMStateDAO extends GenericDAO<FSMState>, FSMDao<ProcessorStateDTO, ExecutionEvents> {

	public static final String FIND_FSM_STATE_BY_NAME = "findByExecutionId";
	public static final String EXECUTION_ID = "execution_id";
	public static final String FIND_ALL_PROC_EXECUTING = "findAllProcessorsExecuting";
	public static final String FIND_PROC_EXECUTING_BY_VALUE = "findProcessorsByValue";
	public static final String FIND_PROC_EXECUTING_BY_DATE = "findProcessorsByDate";
	public static final String FIND_PROC_EXECUTING_BY_VALUE_AND_DATE = "findProcessorsByValueAndDate";
	public static final String FIND_ALL_PROC_EXECUTING_BY_PROCESSORID = "findAllProcessorsExecutingByProcessorId";
	public static final String FIND_MOST_RECENT_SUCCESSFUL_EXECUTION_OF_PROCESSOR = "findMostRecentSuccessfulExecutionOfProcessor";
	public static final String FIND_NON_SLA_VERIFIED_FSM_EVENTS_BY_VALUE = "findNonSLAVerifiedFSMEventsByValue";
	public static final String FIND_NON_SLA_VERIFIED_FILE_STAGED_EVENTS = "findNonSLAVerifiedFileStagedEvents";
	public static final String INTERVAL_IN_HOURS = "interval_in_hours";
	public static final String BY_VALUE = "by_value";
	public static final String FROM_DATE = "from_date";
	public static final String TO_DATE = "to_date";
	public static final String PROCESSOR_ID = "processor_id";
	public static final String FSM_STATE_VALUE = "fsm_state_value";
	public static final String SLA_VERIFICATION_STATUS = "sla_verification_status";
    
	/**
	 * Find FSMState by given status value.
	 * 
	 * @param state
	 * @return FSMState
	 */
	public FSMState find(String state);
	
	/**
	 * Find list of FSMStateValue by given time.
	 * 
	 * @param listJobsIntervalInHours
	 * @return The list of FSMStateValue
	 */
	public List<FSMStateValue> findAllProcessorsExecuting(Timestamp listJobsIntervalInHours);
	
	/**
	 * Find list of FSMStateValue by given status and time.
	 * 
	 * @param value
	 * @param listJobsIntervalInHours
	 * @return The list of FSMStateValue
	 */
	public List<FSMStateValue> findProcessorsExecutingByValue(String value, Timestamp listJobsIntervalInHours);
	
	/**
	 * Find list of FSMStateValue by given date.
	 * 
	 * @param frmDate
	 * @param toDate
	 * @return The list of FSMStateValue
	 */
	public List<FSMStateValue> findProcessorsExecutingByDate(String frmDate, String toDate);
	
	/**
	 * Find list of FSMStateValue by given status and date.
	 * 
	 * @param status
	 * @param frmDate
	 * @param toDate
	 * @return The list of FSMStateValue
	 */
	public List<FSMStateValue> findProcessorsExecutingByValueAndDate(String value, String frmDate, String toDate);
	
	/**
	 * Find list of FSMStateValue by given processorId, status, time Interval
	 * 
	 * @param processorId
	 * @param fsmStateValue
	 * @param timeInterval
	 * 
	 * @return The list of FSMStateValue
	 */
	public List<FSMStateValue> findProcessorsExecutingByProcessorId(String processorId, Timestamp timeInterval);
	
	/**
	 * Find the most recent successful execution of a processor based on given processor Id
	 * 
	 * @param processorId
	 * @return FSMStateValue
	 */
	
	public List<FSMStateValue> findMostRecentSuccessfulExecutionOfProcessor(String processorId);
	
	
	/**
	 * Find the non sla verified events by given fsm state value
	 * 
	 * @param processorId
	 * @return list of FSMState
	 */
	public List <FSMState> findNonSLAVerifiedFSMEventsByValue(String processorId, Timestamp processorLastExecution, String value);
	
	/**
	 * Find the non sla verified events by given fsm state value
	 * 
	 * @param processorId
	 * @return list of FSMState
	 */
	public List <FSMState> findNonSLAVerifiedFileStagedEvents(String processorId, Timestamp processorLastExecution);

}
