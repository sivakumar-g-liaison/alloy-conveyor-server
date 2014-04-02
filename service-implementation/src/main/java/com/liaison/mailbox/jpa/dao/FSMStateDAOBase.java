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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.net.ntp.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.fsm.ActiveEvent;
import com.liaison.fsm.Event;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.model.FSMEvent;
import com.liaison.mailbox.jpa.model.FSMState;
import com.liaison.mailbox.jpa.model.FSMStateValue;
import com.liaison.mailbox.service.core.MailBoxService;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author VNagarajan
 *
 */
public class FSMStateDAOBase extends GenericDAOBase<FSMState> implements FSMStateDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MailBoxService.class);
	
	@Override
	public void addState(String executionId, ProcessorStateDTO state) {
		
		FSMState entity = new FSMState();
		state.copyToEntity(entity);
		persist(entity);
		
		LOGGER.info("The STATE of "+ executionId+" is "+ state.getExecutionState());
	}
	
	@Override
	public ProcessorStateDTO getState(String executionId) {
		
		FSMState state = find(executionId);
		
		//Added annotation to order the items in descending order.
		FSMStateValue value = state.getExecutionState().get(0);
		
		ProcessorStateDTO processorState = new ProcessorStateDTO(state.getExecutionId(), state.getProcessorId(),
				ExecutionState.findByCode(value.getValue()), state.getProcessorName(),
				ProcessorType.findByCode(state.getProcessorType()), state.getMailboxId(),
				state.getProfileName(), state.getStateNotes());
		
		return processorState;
		
	}
	
	@Override
	public Event<ExecutionEvents> createEvent(ExecutionEvents executionEvent) {
		Event<ExecutionEvents> event = new ActiveEvent<ExecutionEvents>(executionEvent);
		return event;
	}
	

	@Override
	public void deleteStates(List<String> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<ExecutionEvents> getEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ProcessorStateDTO> getStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setState(String executionId, ProcessorStateDTO processorState) {
		
		FSMStateValueDAO dao = new FSMStateValueDAOBase();
		FSMState state = find(executionId);
		
		FSMStateValue value = new FSMStateValue();
		value.setPguid(MailBoxUtility.getGUID());
		value.setValue(processorState.getExecutionState().value());
		value.setCreatedDate(MailBoxUtility.getTimestamp());
		value.setFsmState(state);
		
		dao.persist(value);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public FSMState find(String executionId) {
		
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			List<FSMState> states = entityManager.createNamedQuery(FIND_FSM_STATE_BY_NAME)
					.setParameter(EXECUTION_ID, executionId)
					.getResultList();

			Iterator<FSMState> iter = states.iterator();
			while (iter.hasNext()) {
				return iter.next();
			}

		} finally {

			if (entityManager != null) {
				entityManager.close();
			}

		}

		return null;
	}
	

	@Override
	public List<FSMStateValue> findAllProcessorsExecuting(Timestamp listJobsIntervalInHours) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			
			List<FSMStateValue> jobs = new ArrayList<FSMStateValue>();
			
			List<?> jobsRunning = entityManager
								 .createNamedQuery(FIND_ALL_PROC_EXECUTING)
								 .setParameter(INTERVAL_IN_HOURS,listJobsIntervalInHours)
								 .getResultList();

			Iterator<?> iter = jobsRunning.iterator();
			FSMStateValue job;
			while (iter.hasNext()) {
				
				job = (FSMStateValue) iter.next();
				jobs.add(job);
			}
			return jobs;

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}
	
	@Override
	public List<FSMStateValue> findProcessorsExecutingByValue(String value, Timestamp listJobsIntervalInHours) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			
			List<FSMStateValue> jobs = new ArrayList<FSMStateValue>();
			
			List<?> jobsRunning = entityManager
								  .createNamedQuery(FIND_PROC_EXECUTING_BY_VALUE)
								  .setParameter(INTERVAL_IN_HOURS, listJobsIntervalInHours)
								  .setParameter(BY_VALUE, value)
								  .getResultList();

			Iterator<?> iter = jobsRunning.iterator();
			FSMStateValue job;
			while (iter.hasNext()) {
				
				job = (FSMStateValue) iter.next();
				jobs.add(job);
			}
			return jobs;

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}
	
	@Override
	public List<FSMStateValue> findProcessorsExecutingByDate(String frmDate, String toDate) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			
			List<FSMStateValue> jobs = new ArrayList<FSMStateValue>();
			
			List<?> jobsRunning = entityManager
								  .createNamedQuery(FIND_PROC_EXECUTING_BY_DATE)
								  .setParameter(FROM_DATE, frmDate)
								  .setParameter(TO_DATE, toDate)
								  .getResultList();

			Iterator<?> iter = jobsRunning.iterator();
			FSMStateValue job;
			while (iter.hasNext()) {
				
				job = (FSMStateValue) iter.next();
				jobs.add(job);
			}
			return jobs;

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}
	
	@Override
	public List<FSMStateValue> findProcessorsExecutingByValueAndDate(String value, String frmDate, String toDate) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			
			List<FSMStateValue> jobs = new ArrayList<FSMStateValue>();
			
			List<?> jobsRunning = entityManager
								  .createNamedQuery(FIND_PROC_EXECUTING_BY_VALUE_AND_DATE)
								  .setParameter(BY_VALUE, value)
								  .setParameter(FROM_DATE, frmDate)
								  .setParameter(TO_DATE, toDate)
								  .getResultList();

			Iterator<?> iter = jobsRunning.iterator();
			FSMStateValue job;
			while (iter.hasNext()) {
				
				job = (FSMStateValue) iter.next();
				jobs.add(job);
			}
			return jobs;

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}
}
