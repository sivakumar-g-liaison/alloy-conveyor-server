package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.FSMStateValue;



@NamedQueries({
    @NamedQuery(name=FSMStateValueDAO.FIND_FSM_STATE_VALUE_BY_NAME,
            query="SELECT val FROM FSMStateValue val WHERE val.value = :" + FSMStateValueDAO.NAME)
})

public interface FSMStateValueDAO extends GenericDAO <FSMStateValue>{
	
	public static final String FIND_FSM_STATE_VALUE_BY_NAME = "findStateValByName";
	public static final String NAME = "fsmStateValue";
	public FSMStateValue find(String stateValue);

}
