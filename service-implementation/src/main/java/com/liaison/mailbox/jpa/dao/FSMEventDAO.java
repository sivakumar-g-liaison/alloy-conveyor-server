package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.FSMEvent;

@NamedQuery(name = FSMEventDAO.FIND_INTERRUPT_EVENT,
		query = "SELECT eventVal FROM FSMEvent eventVal WHERE eventVal.data =:" + FSMEventDAO.EXECUTION_ID)

public interface FSMEventDAO extends GenericDAO<FSMEvent> {

	public static final String FIND_INTERRUPT_EVENT = "findInterruptEvent";
	public static final String EXECUTION_ID = "execution_id";
	
	public boolean isThereAInterruptSignal(String excutionId);
}
