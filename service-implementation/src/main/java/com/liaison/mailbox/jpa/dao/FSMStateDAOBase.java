package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.FSMState;

public class FSMStateDAOBase extends GenericDAOBase<FSMState> implements FSMStateDAO, MailBoxDAO {

	public FSMStateDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	@SuppressWarnings("unchecked")
	public FSMState find(String state) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			List<FSMState> fsmStates = entityManager.createNamedQuery(FIND_FSM_STATE_BY_NAME).setParameter(NAME, state).getResultList();
			Iterator<FSMState> iter = fsmStates.iterator();

			while (iter.hasNext()) {
				return iter.next();
			}

		} finally {
			if (entityManager != null) {
				entityManager.clear();
			}
		}
		return null;
	}
}
