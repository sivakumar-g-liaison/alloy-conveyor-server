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

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;

import java.util.List;

/**
 * The dao class for the SCHED_PROFILE database table.
 * 
 * @author OFS
 */
public interface ProfileConfigurationDAO extends GenericDAO<ScheduleProfilesRef> {

	String PGUID = "pguid";
	String GET_ALL = "ScheduleProfilesRef.getAll";
	String PROF_NAME = "sch_prof_name";
	String STATUS = "status";
    String TENANCY_KEY = "tenancy_key";
    String PROCESSOR_TYPE = "processorType";

    String GET_PROFILE_BY_NAME = "ScheduleProfilesRef.getProfileByName";
    String FIND_PROFILES_BY_TENANCY_KEY = "ScheduleProfilesRef.findProfilesByTenancyKey";

    /**
     * Find by profileName.
     * 
     * @param profileName profile name
     * @return ScheduleProfilesRef
     */
	ScheduleProfilesRef findProfileByName(String profileName);

	/**
	 * Method to retrieve profiles by given processor type and tenancykey
	 * 
	 * @return list of scheduleProfilesRef
	 */
	List<ScheduleProfilesRef> fetchTransferProfiles(List<String> tenancyKeys);
	
}
