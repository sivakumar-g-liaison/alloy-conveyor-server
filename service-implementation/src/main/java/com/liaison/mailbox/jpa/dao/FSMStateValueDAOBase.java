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

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.FSMStateValue;

public class FSMStateValueDAOBase extends GenericDAOBase<FSMStateValue> implements FSMStateValueDAO, MailBoxDAO {

	public FSMStateValueDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	@SuppressWarnings("unchecked")
	public FSMStateValue find(String stateValue) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			List<FSMStateValue> fsmStateValues = entityManager.createNamedQuery(FIND_FSM_STATE_VALUE_BY_NAME).setParameter(NAME, stateValue).getResultList();
			Iterator<FSMStateValue> iter = fsmStateValues.iterator();

			while (iter.hasNext()) {
				return iter.next();
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return null;
	}
}
