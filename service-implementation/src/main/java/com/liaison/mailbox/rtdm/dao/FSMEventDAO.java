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

import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.FSMEvent;

/**
 * @author OFS
 * 
 */


public interface FSMEventDAO extends GenericDAO<FSMEvent> {

	public static final String FIND_INTERRUPT_EVENT = "FSMEvent.findInterruptEvent";
	public static final String EXECUTION_ID = "execution_id";
	
	/**
	 * Validating by given excutionId.
	 * 
	 * @param excutionId
	 * @return boolean
	 */
	public boolean isThereAInterruptSignal(String excutionId);
}
