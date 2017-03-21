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
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The dao class for the PROCESSOR database table.
 * 
 * @author OFS
 */
public interface ProcessorConfigurationDAO extends GenericDAO<Processor> {

	String FIND_ALL_ACTIVE_PROCESSORS = "Processor.findAllActiveProcessors";
	String FIND_PROCESSOR_BY_NAME_AND_MBX = "Processor.findProcessorByNameAndMbx";
	String FIND_ACTIVE_PROCESSOR_BY_ID = "Processor.findActiveProcessorById";
    String FIND_PROCESSOR_BY_PROFILE_AND_TENANCY = "Processor.findProcessorByProfileAndTenancy";
    String FIND_PROCESSORS_BY_TYPE_AND_MBX_STATUS = "Processor.findProcessorsByType";
    String FIND_PROCESSORS_BY_TYPE_AND_STATUS = "Processor.findProcessorsByTypeAndStatus";
    String FIND_PROCESSOR_BY_NAME = "Processor.findProcessorByName";

	String PROF_NAME = "sch_prof_name";
	String MBX_NAME = "mbx_name";
	String STATUS = "status";
	String STATUS_DELETE = "status_delete";
	String SHARD_KEY = "shard_key";
	String PGUID = "pguid";
	String SERV_INST_ID = "proc_serv_inst_id";
	String PROCESSOR_TYPE = "processor_type";
	String PRCSR_NAME = "prcsr_name";
	String PROFILE_ID = "profile_id";
	String TENANCY_KEY = "tenancy_key";
	String FOLDER_URI = "folder_uri";
	String PROTOCOL = "protocol";
	String PIPELINE_ID = "pipeline_id";
    String MBX_ID = "mbx_id";
    String SCRIPT_NAME ="script_name";
    
    /**
	 * Constants for getProcessor Class
	 */
	String HTTP_SYNC_PRCSR_CLASS = "httpsyncprocessor";
	String HTTP_ASYNC_PRCSR_CLASS = "httpasyncprocessor";
	String SWEEPER_CLASS = "sweeper";
	String FILEWRITER_CLASS = "filewriter";
	String REMOTE_UPLAODER_CLASS = "remoteuploader";
	String REMOTE_DOWNLAODER_CLASS = "remotedownloader";

	String DROPBOX_PRCSR_CLASS = "dropboxprocessor";
	/**
	 * Sorting constants
	 */
	String SORT_MAILBOX_NAME = "mailboxname";
	String SORT_NAME = "name";
	String SORT_PROTOCOL = "protocol";
	String SORT_STATUS = "status";
	String SORT_MAILBOX_STATUS = "mailboxStatus";

	/**
	 * Find by profileName and mailbox name pattern.
	 *
	 * @param profileName    The profile name.
	 * @param mbxNamePattern The MailBox name pattern to exclude
	 * @return The list of processor guids
	 */
	List<String> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern, String shardKey);

	/**
     * Checks the mailbox has the processor or not.
     * 
	 * @param siid service instance id
	 * @param mbxGuid mailbox pguid
	 * @param disableFilter boolean to enable or disable filter
     * @return true if mailbox has the processor otherwise false
     */
	boolean isMailboxHasProcessor(String siid, String mbxGuid, boolean disableFilter);

	/**
	 * Retrieves the list of processor from the given mailbox guid and service instance guid(name).
	 * 
	 * @param mbxGuid pguid of the mailbox
	 * @param siGuid service instance id(name)
	 * @return list of processor
	 */
	Set<Processor> findProcessorByMbxAndServiceInstance(String mbxGuid, String siGuid);

	/**
	 * Retrieves list of processor from the given mailbox guid
	 * 
	 * @param mbxGuid the mailbox guid
	 * @return list of processor
	 */
	Set<Processor> findProcessorByMbx(String mbxGuid, boolean activeEntityRequired);

	/**
	 * Retrieves all active processors of specific types of given mailbox 
	 * 
	 * @param mbxGuid mailbox guid
	 * @param processorTypes - A List of Canonical Names of different types of processor class.
	 * @return list of specific types of processors
	 */
	List<Processor> findActiveProcessorsByTypeAndMailbox(String mbxGuid, List<String> processorTypes);
	
	/**
	 * Retrieves specific type of processors based on the given profile id and Tenancy key
	 * 
	 * @param profileId - Pguid of linked profile
	 * @param tenancyKey - tenancykey of mailbox of the processor
	 * @return list of specific types of processors based on profile id and tenancykey
	 */
	List<Processor> fetchDropboxProcessorsByProfileAndTenancyKey(String profileId, String tenancyKey);
	
	/**
	 * Retrieves all processors of given types and mailbox status
	 * 
	 * @param processorTypes processor types
	 * @param mailboxStatus status mailbox status
	 * @return list of processors
	 */
	List<Processor> findProcessorsByType(List<String> processorTypes, EntityStatus mailboxStatus);

	/**
	 * Retrieves list of all active processors
	 *
	 * @return list of processors
	 */
	List<Processor> findAllActiveProcessors();
	
	/**
	 * Retrieves processors by mailbox guid and processor name
	 *
	 * @param mbxGuid mailbox guid
	 * @param procName processor name
     * @return processors
     */
	Processor findProcessorByNameAndMbx(String mbxGuid, String procName);
 	
 	/**
	 * Retrieves list of all processors  
	 *
	 * @param searchFilter filters
	 * @param pageOffsetDetails page size details
     * @return list of processors
     */
	List<Processor> getAllProcessors(GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);
	
	/**
	 * Retrieve the mailboxes to get mailbox names
	 *
	 * @param searchDTO search details
	 * @return list of mailboxes
     */
	List<MailBox> getMailboxNames(GenericSearchFilterDTO searchDTO);
	
	/**
	 * Retrieve the processors to get the processor names
	 *
	 * @param searchDTO search details
	 * @return list of processor
     */
	List<Processor> getProcessorNames(GenericSearchFilterDTO searchDTO);

	/**
	 * Retrieve the profile names
	 *
	 * @param searchDTO search details
	 * @return list of profiles
	 */
	List<ScheduleProfilesRef> getProfileNames(GenericSearchFilterDTO searchDTO);

	/**
	 * Retrieves count of filtered processors
	 *
	 * @param searchDTO search details
	 * @return count of filtered processors
	 */
	int getFilteredProcessorsCount(GenericSearchFilterDTO searchDTO);

	/**
	 * Retrieves active processor by processor id
	 *
	 * @param id processor guid
	 * @return processor
	 */
	Processor findActiveProcessorById(String id);
 	
	/**
	 * Retrieves all processors by given Name
	 * 
	 * @param processorName processor name
	 * @return List of Processors
	 */
	List<Processor> findProcessorsByName(String processorName);
	
	
	/**
	 * Retrieve processors of specific type by given mailbox Id and type
	 * 
	 * @param mbxId mailbox pguid
	 * @param processorType processor type
	 * @return list of processors
	 */
	List<Object[]> findProcessorsByMailboxIdAndProcessorType(String mbxId, String processorType);
	
	/**
	 * Retrieve processors of specific type by given mailbox Name and type
	 * 
	 * @param mailboxName mailbox name
	 * @param processorType processor type
	 * @return list of processors
	 */
	List<Object[]> findProcessorsByMailboxNameAndProcessorType(String mailboxName, String processorType);

	StringBuilder PROCESSOR_RETRIEVAL_BY_TYPE_AND_MBX_ID_QUERY = new StringBuilder()
		.append("SELECT DISTINCT P.PGUID AS PROCESSOR_GUID, P.TYPE, P.PROTOCOL, P.PROPERTIES, P.STATUS AS PROCSR_STATUS,")
		.append(" PP.NAME AS PROCSR_PROP_NAME, PP.VALUE AS PROC_PROP_VALUE,")
		.append(" SI.SERVICE_INSTANCE_ID,")
		.append(" M.PGUID AS MBX_GUID, M.NAME, M.TENANCY_KEY, M.STATUS AS MBX_STATUS,")
		.append(" MP.NAME AS MBX_PROP_NAME, MP.VALUE AS MBX_PROP_VALUE")
		.append(" FROM PROCESSOR P")
		.append(" LEFT OUTER JOIN PROCESSOR_PROPERTY PP ON PP.PROCESSOR_GUID = P.PGUID")
		.append(" INNER JOIN SERVICE_INSTANCE SI ON SI.PGUID = P.SERVICE_INSTANCE_GUID")
		.append(" INNER JOIN MAILBOX M ON M.PGUID = P.MAILBOX_GUID")
		.append(" LEFT OUTER JOIN MAILBOX_PROPERTY MP ON MP.MAILBOX_GUID = M.PGUID")
		.append(" WHERE P.TYPE = ? AND")
		.append(" M.PGUID = ? AND")
		.append(" P.STATUS = 'ACTIVE' AND")
		.append(" M.STATUS = 'ACTIVE' ")
		.append(" P.CLUSTER_TYPE = ? AND")
		.append(" M.CLUSTER_TYPE = ?");

	StringBuilder PROCESSOR_RETRIEVAL_BY_TYPE_AND_MBX_NAME_QUERY = new StringBuilder()
		.append("SELECT DISTINCT P.PGUID AS PROCESSOR_GUID, P.TYPE, P.PROTOCOL, P.PROPERTIES, P.STATUS AS PROCSR_STATUS,")
		.append(" PP.NAME AS PROCSR_PROP_NAME, PP.VALUE AS PROC_PROP_VALUE,")
		.append(" SI.SERVICE_INSTANCE_ID,")
		.append(" M.PGUID AS MBX_GUID, M.NAME, M.TENANCY_KEY, M.STATUS AS MBX_STATUS,")
		.append(" MP.NAME AS MBX_PROP_NAME, MP.VALUE AS MBX_PROP_VALUE")
		.append(" FROM PROCESSOR P")
		.append(" LEFT OUTER JOIN PROCESSOR_PROPERTY PP ON PP.PROCESSOR_GUID = P.PGUID")
		.append(" INNER JOIN SERVICE_INSTANCE SI ON SI.PGUID = P.SERVICE_INSTANCE_GUID")
		.append(" INNER JOIN MAILBOX M ON M.PGUID = P.MAILBOX_GUID")
		.append(" LEFT OUTER JOIN MAILBOX_PROPERTY MP ON MP.MAILBOX_GUID = M.PGUID")
		.append(" WHERE P.TYPE = ? AND")
		.append(" LOWER(M.NAME) = ? AND")
		.append(" P.STATUS = 'ACTIVE' AND")
		.append(" M.STATUS = 'ACTIVE' AND")
		.append(" P.CLUSTER_TYPE = ? AND")
		.append(" M.CLUSTER_TYPE = ?");

	StringBuilder PROCESSOR_RETRIEVAL_BY_MAILBOX_AND_SIID = new StringBuilder().append("select processor from Processor processor")
			.append(" inner join processor.mailbox mbx")
			.append(" where mbx.pguid = :")
			.append(PGUID)
			.append(" and processor.pguid in (select prcsr.pguid from Processor prcsr")
			.append(" inner join prcsr.serviceInstance si")
			.append(" where si.name like :")
			.append(SERV_INST_ID)
			.append(" and processor.procsrStatus <> :")
			.append(ProcessorConfigurationDAO.STATUS_DELETE)
			.append(" AND processor.clusterType =:")
			.append(MailBoxConstants.CLUSTER_TYPE)
			.append(")");

}
