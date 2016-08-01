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
import com.liaison.mailbox.rtdm.model.FSMStateValue;


/**
 * The dao class for the FSM_STATE_VALUE database table.
 * 
 * @author OFS
 */
public interface FSMStateValueDAO extends GenericDAO <FSMStateValue>{
	
	String FIND_FSM_STATE_VALUE_BY_NAME = "FSMStateValue.findStateValByName";
	String NAME = "fsmStateValue";
	FSMStateValue find(String stateValue);

}
