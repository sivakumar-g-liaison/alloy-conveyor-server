package com.liaison.mailbox.jpa.dao;

import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.Processor;

@NamedQueries({
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN,
				query = "select processor from Processor processor"
						+ " inner join processor.scheduleProfileProcessors schd_prof_processor"
						+ " inner join schd_prof_processor.scheduleProfilesRef profile"
						+ " where profile.schProfName like :" + ProcessorConfigurationDAO.PROF_NAME
						+ " and processor.mailbox.mbxStatus = :" + ProcessorConfigurationDAO.STATUS
						+ " and processor.mailbox.mbxName not like :" + ProcessorConfigurationDAO.MBX_NAME
						+ " and processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS)

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
	 * @return The list of processors.
	 */
	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern);

}
