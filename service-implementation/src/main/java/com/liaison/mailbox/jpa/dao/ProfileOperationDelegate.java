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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.OperationDelegate;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

/**
 * 
 * 
 * @author veerasamyn
 */
public class ProfileOperationDelegate extends OperationDelegate {

	@Override
	@SuppressWarnings("unchecked")
	public List<ScheduleProfilesRef> perform(EntityManager em) {

		List<ScheduleProfilesRef> profileList = new ArrayList<ScheduleProfilesRef>();

		try {

			List<?> profiles = em.createNamedQuery(ProfileConfigurationDAO.GET_ALL).getResultList();
			Iterator<?> iter = profiles.iterator();

			while (iter.hasNext()) {
				profileList.add((ScheduleProfilesRef) iter.next());
			}

		} finally {
			if (em != null) {
				em.clear();
			}
		}
		return profileList;
	}
}
