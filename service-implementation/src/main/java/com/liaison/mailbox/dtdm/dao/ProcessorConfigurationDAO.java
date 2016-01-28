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
import java.util.Map;
import java.util.Set;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;

/**
 * The dao class for the PROCESSOR database table.
 * 
 * @author OFS
 */
public interface ProcessorConfigurationDAO extends GenericDAO<Processor> {

	public static final String FIND_ALL_ACTIVE_PROCESSORS = "Processor.findAllActiveProcessors";
	public static final String FIND_PROCESSOR_BY_NAME_AND_MBX = "Processor.findProcessorByNameAndMbx";
	public static final String FIND_ACTIVE_PROCESSOR_BY_ID = "Processor.findActiveProcessorById";
	public static final String FIND_PROCESSOR_BY_NAME = "Processor.findProcessorByName";

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
	public static final String FOLDER_URI = "folder_uri";
	public static final String PROTOCOL = "protocol";
	public static final String PIPELINE_ID = "pipeline_id";

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
    public boolean isMailboxHasProcessor(String siid, String mbxGuid);

	/**
	 * Retrieves the list of processor from the given mailbox guid and service instance guid(name).
	 * 
	 * @param mbxGuid pguid of the mailbox
	 * @param siGuid service instance id(name)
	 * @return list of processor
	 */
	public Set<Processor> findProcessorByMbxAndServiceInstance(String mbxGuid, String siGuid);

	/**
	 * Retrieves list of processor from the given mailbox guid
	 * 
	 * @param mbxGuid the mailbox guid
	 * @return list of processor
	 */
	public Set<Processor> findProcessorByMbx(String mbxGuid, boolean activeEntityRequired);

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
	public List<Processor> findAllActiveProcessors(); 
	
	/**
 	* Retrieves processors by mailbox guid and processor name
 	* @return  processors
 	*/
 	public Processor findProcessorByNameAndMbx(String mbxGuid, String ProcName); 
 	
 	/**
	 * Retrieves count of all processors  
	 * @return count of processors
	 */
	public int getAllProcessorsCount(); 
	
 	/**
	 * Retrieves list of all processors  
	 * @return list of processors
	 */
	public List<Processor> getAllProcessors(GenericSearchFilterDTO searchFilter, Map <String, Integer> pageOffsetDetails);
	
	/**
	 * Retrieve the mailbox names  
	 * @return list of mailboxes
	 */
	public List<MailBox> getMailboxNames(GenericSearchFilterDTO searchDTO);
	
	/**
	 * Retrieve the processor names  
	 * @return list of processors
	 */
	public List<Processor> getProcessorNames(GenericSearchFilterDTO searchDTO);
	
	/**
	 * Retrieve the profile names  
	 * @return list of profiles
	 */
	public List<ScheduleProfilesRef> getProfileNames(GenericSearchFilterDTO searchDTO);
	
	/**
	 * Retrieves count of filtered processors  
	 * @return count of filtered processors
	 */
	public int getFilteredProcessorsCount(GenericSearchFilterDTO searchDTO);

	/**
 	* Retrieves active processor by processor id
 	* @return  processor
 	*/
 	public Processor findActiveProcessorById(String id);
 	
	/**
	 * Retrieves all processors by given Name
	 * 
	 * @param processorName
	 * @return List of Processors
	 */
	public List<Processor> findProcessorsByName(String processorName);
}
