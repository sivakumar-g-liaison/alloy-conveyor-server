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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;

public class MailBoxConfigurationDAOBase extends GenericDAOBase<MailBox>
		implements MailBoxConfigurationDAO, MailBoxDAO {

	public MailBoxConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	/**
	 * Fetches all MailBox from  MAILBOX database table by given mailbox name, profile name and service instance ids.
	 * 
	 * @param mbxName
	 * @param profName
	 * @return list of mailbox
	 */
	@Override
	public Set<MailBox> find(String mbxName, String profName) {

		Set<MailBox> mailBoxes = new HashSet<MailBox>();

		EntityManager em = DAOUtil.getEntityManager(persistenceUnitName);
		try {

			StringBuffer query = new StringBuffer().append("SELECT mbx FROM MailBox mbx")
					.append(" inner join mbx.mailboxProcessors prcsr")
					.append(" inner join prcsr.scheduleProfileProcessors schd_prof_processor")
					.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME)
					.append(" and profile.schProfName like :" + SCHD_PROF_NAME)
					.append(" order by mbx.mbxName");

			List<?> object = em
					.createQuery(query.toString())
					.setParameter(MBOX_NAME, "%" + (mbxName == null ? "" : mbxName.toLowerCase()) + "%")
					.setParameter(SCHD_PROF_NAME, "%" + (profName == null ? "" : profName) + "%")
					.getResultList();
			Iterator<?> iter = object.iterator();

			while (iter.hasNext()) {
				mailBoxes.add((MailBox) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return mailBoxes;
	}

	/**
	 * Fetches all  MailBox from  MAILBOX database table by given mailbox name.
	 * 
	 * @param mbxName
	 * @return list of mailbox
	 */
	@Override
	public Set<MailBox> findByName(String mbxName) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Set<MailBox> mailBoxes = new HashSet<MailBox>();

		try {

			StringBuffer query = new StringBuffer().append("SELECT mbx FROM MailBox mbx")
					.append(" where LOWER(mbx.mbxName) like :" + MBOX_NAME);
					
			List<?> object = entityManager.createQuery(query.toString())
					.setParameter(MBOX_NAME, "%" + mbxName.toLowerCase() + "%")
					.getResultList();

			Iterator<?> iter = object.iterator();
			while (iter.hasNext()) {
				mailBoxes.add((MailBox) iter.next());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		
		return mailBoxes;
	}

	/**
	 * Generate "in" clause string from the list.
	 *
	 * @param sids list of service instance ids
	 * @return String
	 */
	private String collectionToSqlString(List<String> sids) {

		if (null == sids || sids.isEmpty()) {
			return null;
		}

		StringBuilder s = new StringBuilder();
		for (String str : sids) {
			if (s.length() > 0) {
				s.append(", ");
			}
			s.append(str);
		}

		return s.toString().replaceAll("(\\w+)", "\'$1\'");
	}

}
