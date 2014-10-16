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

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.dtdm.dao.MailBoxDAO;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.rtdm.model.FSMEvent;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 * 
 */
public class FSMEventDAOBase extends  GenericDAOBase<FSMEvent> implements FSMEventDAO, MailboxRTDMDAO {

	public FSMEventDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
	
	/**
	 * Fetches  InterruptEvent from  FSM_EVENT database table.
	 * 
	 * @param excutionId
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean isThereAInterruptSignal(String excutionId) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			
			List<FSMEvent> events = entityManager
								 .createNamedQuery(FIND_INTERRUPT_EVENT)
								 .setParameter(EXECUTION_ID, excutionId)
								 .getResultList();

			Iterator<FSMEvent> iter = events.iterator();
			
			while (iter.hasNext()) {
				return true;
			}
			
			return false;

		} finally {

			if (entityManager != null) {
				entityManager.close();
			}

		}
	}
	
	/**
	 * Method to creates FSMEvent by given ExecutionEvents type and id.
	 * @param executionEvent
	 * @param executionId
	 */
	public void createEvent(ExecutionEvents executionEvent,String executionId) {
		
		FSMEvent fsmEvent = new FSMEvent();
		
		fsmEvent.setPguid(MailBoxUtil.getGUID());
		fsmEvent.setName(executionEvent.toString());
		fsmEvent.setData(executionId);
		fsmEvent.setCreatedDate(MailBoxUtil.getTimestamp());
		persist(fsmEvent);
	}
}
