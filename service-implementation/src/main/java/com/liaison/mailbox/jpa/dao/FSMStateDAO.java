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

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.fsm.FSMDao;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.FSMState;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;

@NamedQueries({ @NamedQuery(name = FSMStateDAO.FIND_FSM_STATE_BY_NAME, 
		query = "SELECT state FROM FSMState state WHERE state.executionId = :" + FSMStateDAO.EXECUTION_ID) })
public interface FSMStateDAO extends GenericDAO<FSMState>, FSMDao<ProcessorStateDTO, ExecutionEvents> {

	public static final String FIND_FSM_STATE_BY_NAME = "findByExecutionId";
	public static final String EXECUTION_ID = "execution_id";

	public FSMState find(String state);

}
