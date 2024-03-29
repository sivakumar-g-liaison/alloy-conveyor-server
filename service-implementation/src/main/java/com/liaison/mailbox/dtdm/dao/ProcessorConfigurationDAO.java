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
import com.liaison.mailbox.service.dto.configuration.ProcessorScriptDTO;

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
    String GET_CLUSTER_TYPE_BY_PROCESSOR_GUID = "Processor.getClusterType";
    String FIND_PROCESSORS_BY_DATACENTER = "Processor.findProcessorsByDatacenter";
    String FIND_SWEEPER_PROCESSORS_FOR_FILE_INGRESS = "Processor.listSweeperProcessors";
    String FIND_PROCESSOR_BY_TYPE_AND_FOLDER_URI = "Processor.listSweeperProcessorsByFolderUri";

    String PROF_NAME = "sch_prof_name";
    String MBX_NAME = "mbx_name";
    String STATUS = "status";
    String STATUS_DELETE = "status_delete";
    String SHARD_KEY = "shard_key";
    String PGUID = "pguid";
    String PGUIDS = "pguids";
    String SERV_INST_ID = "proc_serv_inst_id";
    String PROCESSOR_TYPE = "processor_type";
    String PRCSR_NAME = "prcsr_name";
    String PROFILE_ID = "profile_id";
    String TENANCY_KEY = "tenancy_key";
    String FOLDER_URI = "folder_uri";
    String PROTOCOL = "protocol";
    String PIPELINE_ID = "pipeline_id";
    String MBX_ID = "mbx_id";
    String SCRIPT_NAME = "script_name";
    String DATACENTER = "datacenter";
    String IGNORE_DATACENTERS = "datacenters";
    String UPDATE_SIZE = "updatesize";
    String DATACENTER_NAME = "datacenter_name";
    String PROCESS_DC = "process_dc";
    String EXISTING_PROCESS_DC = "existing_process_dc";
    String NEW_PROCESS_DC = "new_process_dc";
    String CLUSTER_TYPE = "cluster_type";
    String GUID = "guid";
    String NAME = "name";
    String IDP_URI = "idp_uri";

    /**
     * Constants for getProcessor Class
     */
    String HTTP_SYNC_PRCSR_CLASS = "httpsyncprocessor";
    String HTTP_ASYNC_PRCSR_CLASS = "httpasyncprocessor";
    String LITE_HTTP_SYNC_PRCSR_CLASS = "litehttpsyncprocessor";
    String SWEEPER_CLASS = "sweeper";
    String CONDITIONAL_SWEEPER_CLASS = "conditionalsweeper";
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
    String SORT_DIRECTION = "sortDirection";
    String SORT_ACS = "asc";
    String SORT_DESC = "desc";
    String SORT_GUID = "guid";

    /**
     * Find by profileName and mailbox name pattern.
     *
     * @param profileName The profile name.
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
     * Retrieves list of all scriptUI from processors
     *
     *@param searchFilter The GenericSearchFilterDTO
     * @return list of scriptUI
     */
    List<String> getAllScriptURI(GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);
    
    /**
     * Fetch the Processor based on ScriptUI
     * 
     * @param searchFilter The GenericSearchFilterDTO
     * @param pageOffsetDetails
     * @return List of Processor
     */
    List<Processor> getScriptLinkedProcessors(GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails, String filterText);

    /**
     * Retrieves processors count
     *
     * @return list of processors count
     */
    long getProcessorCount();
    
    /**
     * Retrieves processors guid
     *
     * @return list of processors guid
     */
    List<String> getProcessorGuids(String clusterType, List<String> processorTypes);

    /**
     * Retrieves processors guid and process dc
     *
     * @return list of processors guid
     */
    Map<String, String> getProcessorDetails(String clusterType, List<String> processorTypes);

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
     * Retrieves count of filtered ScriptURI
     *
     * @param searchDTO search details
     * @return count of filtered ScriptURI
     */
    int getFilteredProcessorsScriptCount(GenericSearchFilterDTO searchDTO); 
    
    /**
     * Retrieves count of filtered processors based on selected ScriptURI
     *
     * @param searchDTO search details
     * @return count of filtered processors
     */
    int getFilteredScriptLinkedProcessorsCount(GenericSearchFilterDTO searchDTO);

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

    /**
     * Retrieve cluster type based on processorId.
     *
     * @param processorId pguid of the processor
     * @return cluster type of the processor entity
     */
    String getClusterType(String processorId);

    /*
     * Retrieve processorId list by the given processor name.
     * 
     * @param processorName
     * 
     * @return processor ID list
     */
    List<String> getProcessorIdByName(String processorName);

    /**
     * Retrieve processorId by the given processor name and mailbox name.
     *
     * @param processorName
     * @param mailboxName
     * @return processor ID
     */
    String getProcessorIdByProcNameAndMbxName(String mailboxName, String processorName);

    /**
     * Retrieve processor name by the given processor pugid
     *
     * @param pguid
     * @return processorName
     */
    public String getProcessorNameByPguid(String pguid);

    /**
     * Update the Datacenter
     * 
     * @param dc
     * @param processedDC
     * @param processSize
     */
    void updateDatacenter(String dc, List<String> processedDC, int processSize);
    
    /**
     * Update the Processor Datacenter by processor guid
     * 
     * @param dc
     * @param processorGuids
     */
    void updateProcessorDatacenter(String dc, List<String> processorGuids);

    /**
     * Update the process_dc to current_dc where the process_dc is null
     * 
     */
    void updateProcessDc();

    /**
     * Update the process_dc by guid
     * 
     * @param guid
     * @param dataCenter
     */
    void updateProcessDcByGuid(String guid, String dataCenter);
    
    /**
     * Update the existing ProcessDC to new ProcessDC for downloader processor.
     * @param existingProcessDC
     * @param newProcessDC
     * @param processorTypes
     */
    void updateProcessorProcessDc(String existingProcessDC, String newProcessDC, List<String> processorTypes);

    /**
     * Retrieve the all datacenters
     * 
     * @return all datacenters
     */
    List<String> getAllDatacenters();

    /**
     * Retrieve the all processors by dcName
     * 
     * @param dcName
     * @return list of processors
     */
    List<Processor> findProcessorsByDatacenter(String dcName);
    
    /**
     * Retrieve sweeper processors
     * 
     * @return processors
     */
    List<Processor> listSweeperProcessors();

    /**
     * Retrieve sweeper processors based on folder path
     * 
     * @return processors
     */
    List<Processor> listSweeperProcessorsByFolderUri(String folderUri);

    /**
     * Method to update the datacenter
     */
    void updateDatacenters(int option);

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
            .append(" WHERE P.TYPE = ?1 AND")
            .append(" M.PGUID = ?2 AND")
            .append(" P.STATUS <> 'DELETED' AND")
            .append(" M.STATUS <> 'DELETED' AND")
            .append(" P.CLUSTER_TYPE = ?3 AND")
            .append(" M.CLUSTER_TYPE = ?4");

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
            .append(" WHERE P.TYPE = ?1 AND")
            .append(" LOWER(M.NAME) = ?2 AND")
            .append(" P.STATUS <> 'DELETED' AND")
            .append(" M.STATUS <> 'DELETED' AND")
            .append(" P.CLUSTER_TYPE = ?3 AND")
            .append(" M.CLUSTER_TYPE = ?4");

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
            .append(" AND processor.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")")
            .append(")");

    //Don't modify this 3 queries while doing LOW_SECURE changes.
    StringBuilder PROCESSOR_ID_RETRIEVAL_BY_PROCESSOR_NAME = new StringBuilder().append("SELECT pguid FROM processor")
            .append(" WHERE name = ? AND")
            .append(" status = 'ACTIVE' ");

    StringBuilder PROCESSOR_ID_RETRIEVAL_BY_PROCESSOR_NAME_AND_MBX_NAME = new StringBuilder().append("SELECT proc.pguid FROM processor proc")
            .append(" INNER JOIN mailbox mbx")
            .append(" ON mbx.pguid = proc.mailbox_guid")
            .append(" WHERE proc.name = ? AND")
            .append(" mbx.name = ? AND")
            .append(" proc.status = 'ACTIVE' AND mbx.status = 'ACTIVE' ");

    StringBuilder PROCESSOR_NAME_RETRIEVAL_BY_PROCESSOR_ID = new StringBuilder().append("SELECT name FROM processor")
            .append(" WHERE pguid = ? AND")
            .append(" status = 'ACTIVE' ");
    
    String UPDATE_PROCESS_DC = new StringBuilder().append("UPDATE PROCESSOR")
            .append(" SET PROCESS_DC =:" + DATACENTER)
            .append(" WHERE (PROCESS_DC NOT IN (:" + IGNORE_DATACENTERS + ")")
            .append(" OR PROCESS_DC IS NULL) AND STATUS <> 'DELETED' AND rownum <= :")
            .append(UPDATE_SIZE).toString();
    
    String UPDATE_DOWNLOAD_PROCESS_DC = new StringBuilder().append("UPDATE PROCESSOR")
            .append(" SET PROCESS_DC =:" + DATACENTER)
            .append(" WHERE PGUID IN (:" + PGUIDS + ")").toString();
    
    String UPDATE_PROCESS_DC_TO_CURRENT_DC = new StringBuilder().append("UPDATE PROCESSOR")
            .append(" SET PROCESS_DC =:" + DATACENTER)
            .append(" WHERE PROCESS_DC IS NULL AND STATUS <> 'DELETED'")
            .append(" AND PROCESS_DC !='ALL'").toString();

    String UPDATE_PROCESS_DC_BY_GUID = new StringBuilder().append("UPDATE PROCESSOR")
            .append(" SET PROCESS_DC =:" + DATACENTER)
            .append(" WHERE PGUID =:" + PGUID)
            .append(" AND STATUS <> 'DELETED'").toString();
    
    String UPDATE_PROCESSOR_PROCESS_DC = new StringBuilder().append("UPDATE PROCESSOR")
            .append(" SET PROCESS_DC =:" + NEW_PROCESS_DC)
            .append(" WHERE TYPE IN (:" + PROCESSOR_TYPE + ")")
            .append(" AND STATUS <> 'DELETED'")
            .append(" AND PROCESS_DC =:" + EXISTING_PROCESS_DC).toString();
    
    String PROCESSOR_COUNT = new StringBuilder().append("SELECT COUNT(STATUS) FROM PROCESSOR")
            .append(" WHERE STATUS <> 'DELETED' AND PROCESS_DC != 'ALL' ").toString();

    String GET_PROCESSOR_GUIDS = new StringBuilder().append("SELECT PGUID FROM PROCESSOR")
            .append(" WHERE STATUS <> 'DELETED' ")
            .append(" AND CLUSTER_TYPE =:" + CLUSTER_TYPE)
            .append(" AND TYPE in (:" + PROCESSOR_TYPE + ")")
            .append(" AND PROCESS_DC != 'ALL'").toString();

    String GET_PROCESSOR_DETAILS = new StringBuilder().append("SELECT PGUID, PROCESS_DC FROM PROCESSOR")
            .append(" WHERE STATUS <> 'DELETED' ")
            .append(" AND CLUSTER_TYPE =:" + CLUSTER_TYPE)
            .append(" AND TYPE in (:" + PROCESSOR_TYPE + ")")
            .append(" AND PROCESS_DC != 'ALL'").toString();

    String GET_ALL_DATACENTERS = new StringBuilder().append("SELECT DISTINCT PROCESS_DC FROM PROCESSOR")
            .append(" WHERE STATUS <> 'DELETED' ").toString();
  
    StringBuilder PROCESSOR_RETRIEVAL_BY_SCRIPTURI = new StringBuilder().append("SELECT processor FROM Processor processor")
            .append(" WHERE processor.javaScriptUri =:")
            .append(SCRIPT_NAME)
            .append(" AND processor.procsrStatus <> 'DELETED'")
            .append(" AND processor.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")");
    
    StringBuilder GET_PROCESSOR_JAVASCRIPT_URI = new StringBuilder().append("SELECT DISTINCT processor.javaScriptUri FROM Processor processor")
            .append(" WHERE LOWER(processor.javaScriptUri) LIKE :")
            .append(SCRIPT_NAME)
            .append(" AND processor.procsrStatus <> 'DELETED'")
            .append(" AND processor.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")");

    String GET_PROCESSOR_COUNT_BY_JAVASCRIPT_URI = new StringBuilder().append("SELECT COUNT(STATUS) FROM PROCESSOR")
            .append(" WHERE STATUS <> 'DELETED' AND JAVASCRIPT_URI =:" + SCRIPT_NAME)
            .append(" AND CLUSTER_TYPE =:" + MailBoxConstants.CLUSTER_TYPE).toString();
    
    String GET_JAVASCRIPT_URI_COUNT = new StringBuilder().append("SELECT COUNT(DISTINCT JAVASCRIPT_URI) FROM PROCESSOR")
            .append(" WHERE LOWER(JAVASCRIPT_URI) LIKE :")
            .append(SCRIPT_NAME)
            .append(" AND STATUS <> 'DELETED'")
            .append(" AND CLUSTER_TYPE =:" + MailBoxConstants.CLUSTER_TYPE).toString();

}