/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;

import java.util.List;
import java.util.Map;

/**
 * The dao class for the STAGED_FILE database table.
 *
 * @author OFS
 */

public interface StagedFileDAO extends GenericDAO<StagedFile> {

    String FIND_BY_GPID = "StagedFile.findByGpid";

    String FILE_NAME = "fileName";
    String GUID = "file_guid";
    String CURRENT_TIME = "current_time";
    String STATUS = "status";
    String TYPE = "type";
    String PROCESSOR_ID = "processor_id";
    String GLOBAL_PROCESS_ID = "gpid";
    String FILE_PATH = "filePath";
    String EXEC_STATUS = "exec_status";
    String MAILBOX_IDS = "mailbox_ids";
    String GET_STAGED_FILE_BY_FILE_NAME_AND_FILE_PATH = "StagedFile.findStagedFilesByFileNameAndPath";
    String GET_STAGED_FILE_FOR_TRIGGER_FILE = "StagedFile.findStagedFileForTriggerFile";
    String GET_STAGED_FILE_FOR_RELAY_TRIGGER_FILE = "StagedFile.findStagedFileForRelayTriggerFile";
    String MODIFIED_DATE = "modifiedDate";
    String STAGED_FILE_IDS = "stagedFile_ids";
    String PROCESS_DC = "process_dc";
    String EXISTING_PROCESS_DC = "existing_process_dc";
    String NEW_PROCESS_DC = "new_process_dc";
    String PARENT_GLOBAL_PROCESS_ID = "parent_global_process_id";

    /**
     * Method to retrieve the list of all staged files of given mailbox ids
     *
     * @param mailboxIds
     * @return list of stagedFiles
     */
    List<StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);

    List<StagedFile> findStagedFilesOfMailboxesBasedOnGUID(List<String> mailboxIds, String guid);

    int getStagedFilesCountByName(List<String> mailboxIds, String fileName, String status);

    StagedFile findStagedFilesByProcessorId(String processorId, String targetLocation, String fileName);

    StagedFile findStagedFileByProcessorIdAndGpid(String processorId, String gpid);

    List<StagedFile> findStagedFilesByProcessorId(String processorId);
    
    StagedFile findStagedFileByProcessorIdAndFileName(String processorId, String fileName);
    
    long findStagedFilesByParentGlobalProcessId(String parentGlobalProcessId);
    
    StagedFile findStagedFileForTriggerFile(String filePath, String fileName, String processorId);
    
    StagedFile findStagedFileForRelayTriggerFile(String processorId, String triggerFileName);

    StagedFile findStagedFilesByGlobalProcessIdWithoutProcessDc(String parentGlobalProcessId);

    /**
     * constructs staged file entity from workticket and persists it
     *
     * @param workticket workticket
     * @param processorId processor guid
     * @param processorType processor type
     * @param directUploadEnabled boolean to denote direct upload
     */
    void persistStagedFile(WorkTicket workticket, String processorId, String processorType, boolean directUploadEnabled);

    /**
     * list staged files by processor id
     * @param processorId processor id
     * @param directUpload boolean to denote direct upload enabled or not
     * @return list of staged files
     */
    List<StagedFile> findStagedFilesForUploader(String processorId, String filePath, boolean directUpload, boolean recurseSubDir);

    /**
     * get staged file by gpid
     * @param gpid global process id
     * @return staged file
     */
    StagedFile findStagedFileByGpid(String gpid);

    /**
     * Returns staged file entries by filename and file path for file writer processor
     *
     * @param filePath file path
     * @param fileName name
     * @param status status to be excluded
     * @return staged file
     */
    StagedFile findStagedFilesByFileNameAndPath(String filePath, String fileName, String status, List<String> processorTypes);
    
    /**
     * Update the StagedFile Status by processorId
     * 
     * @param processorId
     * @param status
     */
    void updateStagedFileStatusByProcessorId( String processorId,  String status);

    /**
     * Update trigger file entry sttus in StagedFile
     * 
     * @param processorId
     * @param status
     * @param filePath 
     * @param fileName 
     */
    void updateTriggerFileStatusInStagedFile(String processorId, String status, String fileName, String filePath);
    
    /**
     * Update the stagedFile processDC
     * @param existingProcessDC
     * @param newProcessDC
     */
    void updateStagedFileProcessDC(String existingProcessDC,  String newProcessDC);

    /**
     * Update the stagedFile processDC
     * @param newProcessDC
     */
    void updateStagedFileProcessDC(String newProcessDC);
    
    /**
     * 
     * Update trigger file entry status in StagedFile
     * @param processorId
     * @param fileName
     */
    void updateRelayTriggerFileStatusInStagedFile(String processorId, String status, String fileName);

    /**
     * Updated staged files of the remote uploader
     * @param processorGuid processor guid
     * @param newProcessDC new process dc
     */
    void updateStagedFileProcessDCByProcessorGuid(List<String> processorGuid, String newProcessDC);

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_FOR_DIR_UPLOAD_FILE_PATH_RECURSE = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) = :")
            .append(PROCESSOR_ID)
            .append(" and ( sf.filePath LIKE :")
            .append(FILE_PATH)
            .append(" )")
            .append(" and sf.stagedFileStatus IN (:")
            .append(STATUS)
            .append(")")
            .append(" AND sf.clusterType =:")
            .append(MailBoxConstants.CLUSTER_TYPE)
            .append(" AND sf.processDc =:")
            .append(MailBoxConstants.PROCESS_DC)
            .append(")");

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH_RECURSE = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) = :")
            .append(PROCESSOR_ID)
            .append(" and ( sf.filePath LIKE :")
            .append(FILE_PATH)
            .append(" )")
            .append(" and sf.stagedFileStatus != :")
            .append(STATUS)
            .append(" AND sf.clusterType =:")
            .append(MailBoxConstants.CLUSTER_TYPE)
            .append(" AND sf.processDc =:")
            .append(MailBoxConstants.PROCESS_DC)
            .append(")");

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH_DIR_UPLOAD = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) =:")
            .append(PROCESSOR_ID)
            .append(" and sf.filePath =:")
            .append(FILE_PATH)
            .append(" and sf.stagedFileStatus IN (:")
            .append(StagedFileDAO.STATUS)
            .append(")")
            .append(" AND sf.clusterType =:")
            .append(MailBoxConstants.CLUSTER_TYPE)
            .append(" AND sf.processDc =:")
            .append(MailBoxConstants.PROCESS_DC)
            .append(")");
    
    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_AND_DIR_UPLOAD = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) =:")
            .append(PROCESSOR_ID)
            .append(" and sf.stagedFileStatus IN (:")
            .append(StagedFileDAO.STATUS)
            .append(")")
            .append(" AND sf.clusterType =:")
            .append(MailBoxConstants.CLUSTER_TYPE)
            .append(" AND sf.processDc =:")
            .append(MailBoxConstants.PROCESS_DC)
            .append(")");

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) =:")
            .append(PROCESSOR_ID)
            .append(" and sf.filePath =:")
            .append(FILE_PATH)
            .append(" and sf.stagedFileStatus !=:")
            .append(StagedFileDAO.STATUS)
            .append(" AND sf.clusterType =:")
            .append(MailBoxConstants.CLUSTER_TYPE)
            .append(" AND sf.processDc =:")
            .append(MailBoxConstants.PROCESS_DC)
            .append(")");

    StringBuilder FIND_STAGED_FILE = new StringBuilder()
            .append("select sf from StagedFile sf")
            .append(" where sf.processorId =:")
            .append(PROCESSOR_ID)
            .append(" and sf.stagedFileStatus in (:")
            .append(STATUS)
            .append(") and sf.fileName =:")
            .append(FILE_NAME)
            .append(" and sf.filePath =:")
            .append(FILE_PATH)
            .append(" AND sf.clusterType =:")
            .append(MailBoxConstants.CLUSTER_TYPE)
            .append(" order by sf.createdDate desc");
    
    String UPDATE_STAGED_FILE_STATUS_BY_PROCESSORID = new StringBuilder()
            .append("UPDATE STAGED_FILE")
            .append(" SET STATUS =:" + STATUS)
            .append(" WHERE PROCESSOR_GUID =:")
            .append(PROCESSOR_ID).toString();
    
    String UPDATE_TRIGGER_FILE_STATUS_IN_STAGED_FILE = new StringBuilder()
            .append("UPDATE STAGED_FILE")
            .append(" SET STATUS =:" + STATUS)
            .append(" WHERE PROCESSOR_GUID =:")
            .append(PROCESSOR_ID)
            .append(" AND FILE_PATH =:")
            .append(FILE_PATH)
            .append(" AND FILE_NAME =:")
            .append(FILE_NAME).toString();
    
    String UPDATE_STAGED_FILE_BY_PROCESS_DC = new StringBuilder()
            .append("UPDATE STAGED_FILE")
            .append(" SET PROCESS_DC =:" + NEW_PROCESS_DC)
            .append(" WHERE PROCESS_DC =:" + EXISTING_PROCESS_DC)
            .append(" AND STATUS <> 'INACTIVE'").toString();
    
    String UPDATE_STAGED_FILE_PROCESS_DC = new StringBuilder()
            .append("UPDATE STAGED_FILE")
            .append(" SET PROCESS_DC =:" + NEW_PROCESS_DC)
            .append(" WHERE STATUS <> 'INACTIVE'").toString();

    String UPDATE_STAGED_FILE_PROCESS_DC_PROCESSOR_GUID = new StringBuilder()
            .append("UPDATE STAGED_FILE")
            .append(" SET PROCESS_DC =:" + NEW_PROCESS_DC)
            .append(" WHERE STATUS <> 'INACTIVE'")
            .append(" AND PROCESSOR_GUID IN (:" + PROCESSOR_ID + ")").toString();
   
    String FIND_STAGED_FILE_BY_GPID_AND_PROCESSID = new StringBuilder()
            .append("SELECT sf FROM StagedFile sf")
            .append(" WHERE sf.processorId =:")
            .append(PROCESSOR_ID)
            .append(" AND sf.globalProcessId =:")
            .append(GLOBAL_PROCESS_ID)
            .append(" AND sf.stagedFileStatus IN (:")
            .append(STATUS)
            .append(")").toString();
    
    String FIND_STAGED_FILE_BY_PROCESSID = new StringBuilder()
            .append("SELECT sf FROM StagedFile sf")
            .append(" WHERE sf.processorId =:")
            .append(PROCESSOR_ID)
            .append(" AND sf.stagedFileStatus IN (:")
            .append(STATUS)
            .append(")")
            .append(" AND sf.processDc =:")
            .append(PROCESS_DC).toString();
    
    String FIND_STAGED_FILES_BY_PROCESSID_AND_NAME = new StringBuilder()
            .append("SELECT sf FROM StagedFile sf")
            .append(" WHERE sf.processorId =:")
            .append(PROCESSOR_ID)
            .append(" AND sf.stagedFileStatus IN (:")
            .append(STATUS)
            .append(") AND sf.fileName =:")
            .append(FILE_NAME)
            .append(" AND sf.processDc =:")
            .append(PROCESS_DC).toString();

    String FIND_STAGED_FILES_BY_PARENT_GLOBAL_PROCESS_ID = new StringBuilder()
            .append("select count(sf.pguid) from StagedFile sf")
            .append(" where sf.parentGlobalProcessId =:")
            .append(PARENT_GLOBAL_PROCESS_ID).toString();

    String FIND_STAGED_FILE_BY_GLOBAL_PROCESS_ID_WITHOUT_PROCESS_DC = new StringBuilder()
            .append("select sf from StagedFile sf")
            .append(" where sf.globalProcessId =:")
            .append(GLOBAL_PROCESS_ID)
            .toString();

    String UPDATE_RELAY_TRIGGER_FILE_STATUS_IN_STAGED_FILE = new StringBuilder()
            .append("UPDATE STAGED_FILE")
            .append(" SET STATUS =:" + STATUS)
            .append(" WHERE PROCESSOR_GUID =:")
            .append(PROCESSOR_ID)
            .append(" AND FILE_NAME =:")
            .append(FILE_NAME).toString();

}
