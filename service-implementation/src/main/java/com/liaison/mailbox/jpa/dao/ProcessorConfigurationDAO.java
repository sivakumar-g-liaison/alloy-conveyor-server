package com.liaison.mailbox.jpa.dao;

import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.Processor;

@NamedQueries({
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PGUID,
				query = "SELECT processor FROM Processor processor WHERE processor.pguid = :" + ProcessorConfigurationDAO.PGUID),
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PROFILE,
				query = "select processor from Processor processor "
						+ "inner join processor.mailboxSchedProfile profile "
						+ "inner join profile.scheduleProfilesRef schdprof "
						+ "inner join profile.mailbox mbx "
						+ "where schdprof.schProfName =:"
						+ ProcessorConfigurationDAO.PROF_NAME
		),
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN,
				query = "select processor from Processor processor "
						+ "inner join processor.mailboxSchedProfile profile "
						+ "inner join profile.scheduleProfilesRef schdprof "
						+ "inner join profile.mailbox mbx "
						+ "where schdprof.schProfName = :"
						+ ProcessorConfigurationDAO.PROF_NAME
						+ " and mbx.mbxName not like :" + ProcessorConfigurationDAO.MBX_NAME + "")
})
public interface ProcessorConfigurationDAO extends GenericDAO<Processor> {

	public static final String FIND_PROCESSOR_BY_PGUID = "findProcessorByPguid";
	public static final String FIND_PROCESSOR_BY_PROFILE = "findProcessorByProfile";
	public static final String FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN = "findProcessorByProfileAndMbxNamePattern";

	public static final String PGUID = "pguid";
	public static final String PROF_NAME = "sch_prof_name";
	public static final String MBX_NAME = "mbx_name";

	public Processor find(String guid);

	public List<Processor> findByProfile(String profileName);

	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern);

	public void deactivate(String guId);
}
