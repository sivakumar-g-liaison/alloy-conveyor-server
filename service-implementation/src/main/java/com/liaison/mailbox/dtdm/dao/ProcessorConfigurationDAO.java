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

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ProcessorType;

/**
 * @author OFS
 * 
 */
@NamedQueries({
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN,
				query = "select processor from Processor processor"
						+ " inner join processor.scheduleProfileProcessors schd_prof_processor"
						+ " inner join schd_prof_processor.scheduleProfilesRef profile"
						+ " where profile.schProfName like :" + ProcessorConfigurationDAO.PROF_NAME
						+ " and processor.mailbox.mbxStatus = :" + ProcessorConfigurationDAO.STATUS
						+ " and processor.mailbox.mbxName not like :" + ProcessorConfigurationDAO.MBX_NAME
						+ " and processor.mailbox.shardKey like :" + ProcessorConfigurationDAO.SHARD_KEY
						+ " and processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS
						+ " order by " + ProcessorConfigurationDAO.PROF_NAME), 
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_COUNT,
						query = "select count(processor) from Processor processor"
								+ " inner join processor.mailbox mbx"
								+ " where mbx.pguid = :" + ProcessorConfigurationDAO.PGUID),
		@NamedQuery(name = ProcessorConfigurationDAO.FIND_ALL_ACTIVE_PROCESSORS,
						query = "select processor from Processor processor"
								+ " where processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS)
})
public interface ProcessorConfigurationDAO extends GenericDAO<Processor> {

	public static final String FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN = "findProcessorByProfileAndMbxNamePattern";
	public static final String FIND_PROCESSOR_COUNT = "findProcessorCountByMailboxId";
	public static final String FIND_ALL_ACTIVE_PROCESSORS = "findAllActiveProcessors";

	public static final String PROF_NAME = "sch_prof_name";
	public static final String MBX_NAME = "mbx_name";
	public static final String STATUS = "status";
	public static final String SHARD_KEY = "shard_key";
	public static final String PGUID = "pguid";
	public static final String SERV_INST_ID = "proc_serv_inst_id";
	public static final String PROCESSOR_TYPE = "processor_type";

	/**
	 * Find by profileName and mailbox name pattern.
	 * 
	 * @param profileName
	 *            The profile name.
	 * @param mbxNamePattern
	 *            The MailBox name pattern to exclude
	 * @return The list of processors.
	 */
	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern, String shardKey);

	/**
	 * Checks the mailbox has the processor or not.
	 * 
	 * @param guid pguid of the mailbox
	 * @return boolean
	 */
	public boolean isMailboxHasProcessor(String guid);

	/**
	 * Retrieves the list of processor from the given mailbox guid and service instance guid(name).
	 * 
	 * @param mbxGuid pguid of the mailbox
	 * @param siGuid service instance id(name)
	 * @return list of processor
	 */
	public List<Processor> findProcessorByMbxAndServiceInstance(String mbxGuid, String siGuid);

	/**
	 * Retrieves list of processor from the given mailbox guid
	 * 
	 * @param mbxGuid the mailbox guid
	 * @return list of processor
	 */
	public List<Processor> findProcessorByMbx(String mbxGuid);
	
	/**
	 * Retrieves list of all processors of specific type from given mailbox guid
	 * 
	 * @param type the processor type
	 * @param mbxGuid the mailbox guid
	 * @return list of processors
	 */
	public List <Processor> findProcessorByTypeAndMbx(ProcessorType type, String mbxGuid);
	
	
	/**
	 * Retrieves list of all processors of specific type from given mailbox guid
	 * 
	 * @param type the processor type
	 * @param mbxGuid the mailbox guid
	 * @return list of processors
	 */
	public List <Processor> findProcessorByType(ProcessorType type);
	
	/**
	 * Retrieves list of all processors  
	 * @return list of processors
	 */
	public List <Processor> findAllActiveProcessors(); 

}
