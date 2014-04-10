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

@NamedQueries({ @NamedQuery(name = FSMStateDAO.FIND_FSM_STATE_BY_NAME, 
		query = "SELECT state FROM FSMState state WHERE state.executionId = :" + FSMStateDAO.EXECUTION_ID),
@NamedQuery(name = FSMStateDAO.FIND_ALL_PROC_EXECUTING,
		query = "select stateVal from FSMStateValue stateVal"
				+ " where stateVal.createdDate IN (select max(staVal.createdDate) from FSMState sta"
				+ " inner join sta.executionState staVal"
				+ " where staVal.createdDate >= :" + FSMStateDAO.INTERVAL_IN_HOURS + " group by staVal.fsmState)"),
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
                + " group by staVal.fsmState)")
})

public interface FSMStateDAO extends GenericDAO<FSMState>, FSMDao<ProcessorStateDTO, ExecutionEvents> {

	public static final String FIND_FSM_STATE_BY_NAME = "findByExecutionId";
	public static final String EXECUTION_ID = "execution_id";
	public static final String FIND_ALL_PROC_EXECUTING = "findAllProcessorsExecuting";
	public static final String FIND_PROC_EXECUTING_BY_VALUE = "findProcessorsByValue";
	public static final String FIND_PROC_EXECUTING_BY_DATE = "findProcessorsByDate";
	public static final String FIND_PROC_EXECUTING_BY_VALUE_AND_DATE = "findProcessorsByValueAndDate";
	public static final String INTERVAL_IN_HOURS = "interval_in_hours";
	public static final String BY_VALUE = "by_value";
	public static final String FROM_DATE = "from_date";
	public static final String TO_DATE = "to_date";
    
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

}
