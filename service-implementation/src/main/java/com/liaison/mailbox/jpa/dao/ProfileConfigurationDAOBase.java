package com.liaison.mailbox.jpa.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

public class ProfileConfigurationDAOBase extends GenericDAOBase<ScheduleProfilesRef> implements ProfileConfigurationDAO,
		MailBoxDAO {

	public ProfileConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

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
				entityManager.clear();
			}
		}
		return null;
	}
	
	@Override
	public List <ScheduleProfilesRef> findProfilesByName(String profileName) {
		
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		
		List<ScheduleProfilesRef> profileList = new ArrayList<ScheduleProfilesRef>();
		
		try {
			
			@SuppressWarnings("unchecked")
			List<ScheduleProfilesRef> profiles = entityManager.createNamedQuery(FIND_PROFILES_BY_NAME)
					.setParameter(PROF_NAME, "%" + profileName.toLowerCase() + "%").getResultList();
			Iterator<?> iter = profiles.iterator();

			while (iter.hasNext()) {
				profileList.add((ScheduleProfilesRef) iter.next());
			}
			
		} finally {
			if (entityManager != null) {
				entityManager.clear();
			}
		}
		return profileList;
	}
}
