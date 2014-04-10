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

import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

@NamedQueries({
		@NamedQuery(name = ProfileConfigurationDAO.GET_PROFILE_BY_NAME, query = "select schdprof from ScheduleProfilesRef schdprof where schdprof.schProfName = :"
				+ ProfileConfigurationDAO.PROF_NAME),
		@NamedQuery(name = ProfileConfigurationDAO.GET_ALL, query = "select schdprof from ScheduleProfilesRef schdprof order by schdprof.schProfName"),
		@NamedQuery(name = ProfileConfigurationDAO.FIND_PROFILES_BY_NAME,
		query = "SELECT schdprof FROM ScheduleProfilesRef schdprof where LOWER(schdprof.schProfName) like :" + ProfileConfigurationDAO.PROF_NAME)})
public interface ProfileConfigurationDAO extends GenericDAO<ScheduleProfilesRef> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL = "getAll";
	public static final String PROF_NAME = "sch_prof_name";
	public static final String GET_PROFILE_BY_NAME = "getProfileByName";
	public static final String FIND_PROFILES_BY_NAME = "findProfilesByName";
	
    /**
     * Find by profileName.
     * 
     * @param profileName
     * @return ScheduleProfilesRef
     */
	public ScheduleProfilesRef findProfileByName(String profileName);
	
	/**
	 * Find by profileName.
	 * 
	 * @param profileName
	 * @return The list of ScheduleProfilesRef
	 */
	public List <ScheduleProfilesRef> findProfilesByName(String profileName);
}
