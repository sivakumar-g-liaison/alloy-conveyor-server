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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.service.util.QueryBuilderUtil;

/**
 * @author OFS
 * 
 */
public class ProfileConfigurationDAOBase extends GenericDAOBase<ScheduleProfilesRef> implements ProfileConfigurationDAO,
		MailboxDTDMDAO {

	private static final Logger LOG = LoggerFactory.getLogger(ProfileConfigurationDAOBase.class);
	
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
	
	@Override
	public List<ScheduleProfilesRef> findTransferProfilesSpecificProcessorTypeByTenancyKey(String tenancyKey, List<String> specificProcessorTypes) {
			
			EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			List<ScheduleProfilesRef> processors = new ArrayList<ScheduleProfilesRef>();
			
			try {
				LOG.info("Fetching the transfer profiles by specific processor type and tenancyKey starts.");
				
				/*StringBuilder query = new StringBuilder().append("select distinct schdprof from ScheduleProfilesRef schdprof")
							.append(" inner join schdprof.scheduleProfileProcessors schd_prof_processor")
							.append(" where schd_prof_processor.processor.pguid IN (select procsr.pguid from Processor procsr")
							.append(" where procsr.procsrStatus = :" + ProfileConfigurationDAO.STATUS)
							.append(" and procsr.mailbox.tenancyKey = :" + ProfileConfigurationDAO.TENANCY_KEY)
							.append(" and procsr.mailbox.mbxStatus = :" + ProfileConfigurationDAO.STATUS)
							.append(" and ( " + constructSqlStringForTypeOperator(specificProcessorTypes) + "))");*/
				
				StringBuilder query = new StringBuilder().append("select distinct profile from Processor processor")
							.append(" inner join processor.scheduleProfileProcessors schd_prof_processor")
							.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
							.append(" where processor.mailbox.tenancyKey = :" + ProfileConfigurationDAO.TENANCY_KEY)
							.append(" and processor.mailbox.mbxStatus = :" + ProfileConfigurationDAO.STATUS)
							.append(" and processor.procsrStatus = :" + ProfileConfigurationDAO.STATUS)
							.append(" and ( " + QueryBuilderUtil.constructSqlStringForTypeOperator(specificProcessorTypes) + ")");
						
				
				List<?> proc = entityManager.createQuery(query.toString())
						.setParameter(ProfileConfigurationDAO.TENANCY_KEY, tenancyKey)
						.setParameter(ProfileConfigurationDAO.STATUS, MailBoxStatus.ACTIVE.name())
						.getResultList();
	
				Iterator<?> iter = proc.iterator();
				ScheduleProfilesRef transferProfile;
				while (iter.hasNext()) {
	
					transferProfile = (ScheduleProfilesRef) iter.next();
					processors.add(transferProfile);
					LOG.info("Transfer profile -Pguid : {}, profileName : {}",
							transferProfile.getPrimaryKey(), transferProfile.getSchProfName());
				}
						
			} finally {
				if (entityManager != null) {
					entityManager.close();
				}
			}
			return processors;
		}	

}
