package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.FSMEvent;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class FSMEventDAOBase extends  GenericDAOBase<FSMEvent> implements FSMEventDAO, MailBoxDAO {

	public FSMEventDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
	
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
	
	public void createEvent(ExecutionEvents executionEvent,String executionId) {
		
		FSMEvent fsmEvent = new FSMEvent();
		
		fsmEvent.setPguid(MailBoxUtility.getGUID());
		fsmEvent.setName(executionEvent.toString());
		fsmEvent.setData(executionId);
		fsmEvent.setCreatedDate(MailBoxUtility.getTimestamp());
		persist(fsmEvent);
	}
}
