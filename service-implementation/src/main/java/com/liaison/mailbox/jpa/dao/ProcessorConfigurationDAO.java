package com.liaison.mailbox.jpa.dao;

import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.Processor;

@NamedQueries({
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN,
				query = "select processor from Processor processor "
						+ "inner join processor.mailboxSchedProfile profile "
						+ "inner join profile.scheduleProfilesRef schdprof "
						+ "inner join profile.mailbox mbx "
						+ "where schdprof.schProfName = :" + ProcessorConfigurationDAO.PROF_NAME
						+ " and mbx.mbxName not like :" + ProcessorConfigurationDAO.MBX_NAME
						+ " and mbx.mbxStatus = :" + ProcessorConfigurationDAO.STATUS
						+ " and profile.mbxProfileStatus = :" + ProcessorConfigurationDAO.STATUS
						+ " order by mbx.mbxName, processor.executionOrder")
})
public interface ProcessorConfigurationDAO extends GenericDAO<Processor> {

	public static final String FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN = "findProcessorByProfileAndMbxNamePattern";

	public static final String PROF_NAME = "sch_prof_name";
	public static final String MBX_NAME = "mbx_name";
	public static final String STATUS = "status";

	/**
	 * Find by profileName and mailbox name pattern.
	 * 
	 * @param profileName
	 *            The profile name.
	 * @param mbxNamePattern
	 *            The MailBox name pattern to exclude
	 * @return
	 */
	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern);

	/**
	 * Deactivate the processor.
	 * 
	 * @param guId
	 *            The primary key
	 */
	// public void deactivate(String guId);
}
