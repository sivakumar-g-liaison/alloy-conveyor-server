/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.dao;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.dtdm.model.DropBoxProcessor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ProcessorType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains the profile fetch informations and  We can retrieve the profile details here.
 * 
 * @author OFS
 */
public class ProfileConfigurationDAOBase extends GenericDAOBase<ScheduleProfilesRef> implements ProfileConfigurationDAO,
		MailboxDTDMDAO {

	private static final Logger LOG = LogManager.getLogger(ProfileConfigurationDAOBase.class);

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

		EntityManager entityManager = null;
		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
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

    @Override
    @SuppressWarnings("unchecked")
    public List<ScheduleProfilesRef> fetchTransferProfiles(List<String> tenancyKeys) {

        EntityManager entityManager = null;
        List<ScheduleProfilesRef> profiles = new ArrayList<ScheduleProfilesRef>();

        try {
            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            profiles = entityManager.createNamedQuery(FIND_PROFILES_BY_TENANCY_KEY)
                    .setParameter(ProfileConfigurationDAO.TENANCY_KEY, tenancyKeys)
                    .setParameter(ProfileConfigurationDAO.STATUS, EntityStatus.ACTIVE.name())
                    .setParameter(PROCESSOR_TYPE, ProcessorType.DROPBOXPROCESSOR.name())
                    .getResultList();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return profiles;
    }

}
