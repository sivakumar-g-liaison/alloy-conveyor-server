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

/**
 * @author OFS
 * 
 */
public interface ProcessorConfigurationDAO extends GenericDAO<Processor> {

	public static final String FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN = "Processor.findProcessorByProfileAndMbxNamePattern";
	public static final String FIND_PROCESSOR_COUNT = "Processor.findProcessorCountByMailboxId";
	public static final String FIND_ALL_ACTIVE_PROCESSORS = "Processor.findAllActiveProcessors";
	public static final String FIND_PROCESSOR_BY_NAME_AND_MBX = "Processor.findProcessorByNameAndMbx";

	public static final String PROF_NAME = "sch_prof_name";
	public static final String MBX_NAME = "mbx_name";
	public static final String STATUS = "status";
	public static final String SHARD_KEY = "shard_key";
	public static final String PGUID = "pguid";
	public static final String SERV_INST_ID = "proc_serv_inst_id";
	public static final String PROCESSOR_TYPE = "processor_type";
	public static final String PRCSR_NAME = "prcsr_name";
	public static final String PROFILE_ID = "profile_id";
	public static final String TENANCY_KEY = "tenancy_key";

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
	public List<Processor> findProcessorByMbx(String mbxGuid, boolean activeEntityRequired);

	/**
	 * Retrieves all active processors of specific types of given mailbox 
	 * 
	 * @param mbxGuid
	 * @param specificProcessorTypes - A List of Canonical Names of different types of processor class.
	 * @return list of specific types of processors
	 */
	public List<Processor> findSpecificProcessorTypesOfMbx(String mbxGuid, List<String>specificProcessorTypes);
	
	/**
	 * Retrieves specific type of processors based on the given profile id and Tenancy key
	 * 
	 * @param profileId - Pguid of linked profile
	 * @param tenancyKey - tenancykey of mailbox of the processor
	 * @param specificProcessorTypes A List of Canonical Names of different types of processor class.
	 * @return list of specific types of processors based on profile id and tenancykey
	 */
	public List<Processor> findProcessorsOfSpecificTypeByProfileAndTenancyKey(String profileId, String tenancyKey, List<String> specificProcessorTypes);
	
	/**
	 * Retrieves all active processors of given types
	 * 
	 * @param specificProcessorTypes
	 * @return
	 */
	public List<Processor> findProcessorsByType(List<String> specificProcessorTypes);
	
	/**
	 * Retrieves list of all processors  
	 * @return list of processors
	 */
	public List <Processor> findAllActiveProcessors(); 
	
	/**
 	* Retrieves processors by mailbox guid and processor name
 	* @return  processors
 	*/
 	public Processor findProcessorByNameAndMbx(String mbxGuid, String ProcName); 


}
