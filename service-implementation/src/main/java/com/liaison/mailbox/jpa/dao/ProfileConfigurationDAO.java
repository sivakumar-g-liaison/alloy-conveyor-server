package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.ScheduleProfilesRef;

import java.util.List;

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
	

	public ScheduleProfilesRef findProfileByName(String profileName);
	public List <ScheduleProfilesRef> findProfilesByName(String profileName);
}
