package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.FSMState;

@NamedQueries({ @NamedQuery(name = FSMStateDAO.FIND_FSM_STATE_BY_NAME, query = "SELECT state FROM FSMState state WHERE state.name = :"
		+ FSMStateDAO.NAME) })

public interface FSMStateDAO extends GenericDAO<FSMState> {

	public static final String FIND_FSM_STATE_BY_NAME = "findByFSMStateName";
	public static final String NAME = "fsmStateName";

	public FSMState find(String state);

}
