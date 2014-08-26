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
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

/**
 * @author OFS
 * 
 */
public class ProfileConfigurationDAOBase extends GenericDAOBase<ScheduleProfilesRef> implements ProfileConfigurationDAO,
		MailBoxDAO {

	public ProfileConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
    
	/**
	 * Fetches  ScheduleProfilesRef from SCHED_PROFILE database table by given profileName.
	 * 
	 * @param profileName.
	 * @return ScheduleProfilesRef
	 */
	@Override
	public ScheduleProfilesRef findProfileByName(String profileName) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			@SuppressWarnings("unchecked")
			List<ScheduleProfilesRef> profiles = entityManager.createNamedQuery(GET_PROFILE_BY_NAME)
					.setParameter(PROF_NAME, profileName).getResultList();
			Iterator<ScheduleProfilesRef> iter = profiles.iterator();

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
