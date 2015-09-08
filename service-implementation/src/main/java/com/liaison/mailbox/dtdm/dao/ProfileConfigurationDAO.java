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

import java.util.List;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;

/**
 * The dao class for the SCHED_PROFILE database table.
 * 
 * @author OFS
 */
public interface ProfileConfigurationDAO extends GenericDAO<ScheduleProfilesRef> {

	public static final String PGUID = "pguid";
	public static final String GET_ALL = "ScheduleProfilesRef.getAll";
	public static final String PROF_NAME = "sch_prof_name";
	public static final String GET_PROFILE_BY_NAME = "ScheduleProfilesRef.getProfileByName";
	public static final String STATUS = "status";
	public static final String TENANCY_KEY = "tenancy_key";
	
    /**
     * Find by profileName.
     * 
     * @param profileName
     * @return ScheduleProfilesRef
     */
	public ScheduleProfilesRef findProfileByName(String profileName);
	
	/**
	 * Method to retrieve profiles by given processor type and tenancykey
	 * 
	 * @param tenancyKey
	 * @param specificProcessorTypes
	 * @return list of scheduleProfilesRef
	 */
	public List<ScheduleProfilesRef> findTransferProfilesSpecificProcessorTypeByTenancyKey(String tenancyKey, List<String> specificProcessorTypes);
	
}
