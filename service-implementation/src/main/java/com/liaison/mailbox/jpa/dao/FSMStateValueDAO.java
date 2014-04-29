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
import com.liaison.mailbox.jpa.model.FSMStateValue;


/**
 * @author OFS
 * 
 */
@NamedQueries({
    @NamedQuery(name=FSMStateValueDAO.FIND_FSM_STATE_VALUE_BY_NAME,
            query="SELECT val FROM FSMStateValue val WHERE val.value = :" + FSMStateValueDAO.NAME)
})

public interface FSMStateValueDAO extends GenericDAO <FSMStateValue>{
	
	public static final String FIND_FSM_STATE_VALUE_BY_NAME = "findStateValByName";
	public static final String NAME = "fsmStateValue";
	public FSMStateValue find(String stateValue);

}
