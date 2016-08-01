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
import com.liaison.mailbox.rtdm.model.FSMEvent;

/**
 * The dao class for the FSM_EVENT database table.
 * 
 * @author OFS
 */


public interface FSMEventDAO extends GenericDAO<FSMEvent> {

	String FIND_INTERRUPT_EVENT = "FSMEvent.findInterruptEvent";
	String EXECUTION_ID = "execution_id";
	
	/**
	 * Validating by given excutionId.
	 * 
	 * @param excutionId
	 * @return boolean
	 */
	boolean isThereAInterruptSignal(String excutionId);
}
