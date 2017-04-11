/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.dto.queue.WorkTicket;
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
    String GET_STAGED_FILE_BY_FILE_NAME_AND_FILE_PATH_FOR_FILE_WRITER = "StagedFile.findStagedFilesForFileWriterByFileNameAndPath";
    String MODIFIED_DATE = "modifiedDate";
    String STAGED_FILE_IDS = "stagedFile_ids";

	/**
	 * Method to retrieve the list of all staged files of given mailbox ids
	 *
	 * @param mailboxIds
	 * @return list of stagedFiles
	 */
	List <StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);
	List <StagedFile> findStagedFilesOfMailboxesBasedonGUID(List<String> mailboxIds, String guid);
	int getStagedFilesCountByName(List<String> mailboxIds, String fileName,String status);
	StagedFile findStagedFilesByProcessorId(String processorId, String targetLocation, String fileName);

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
     * @return staged file
     */
    StagedFile findStagedFilesForFileWriterByFileNameAndPath(String filePath, String fileName);

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_FOR_DIR_UPLOAD_FILE_PATH_RECURSE = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) = :")
            .append(PROCESSOR_ID)
            .append(" and ( sf.filePath LIKE :")
            .append(FILE_PATH)
            .append(" )")
            .append(" and sf.stagedFileStatus IN (:")
            .append(STATUS)
            .append("))");

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH_RECURSE = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) = :")
            .append(PROCESSOR_ID)
            .append(" and ( sf.filePath LIKE :")
            .append(FILE_PATH)
            .append(" )")
            .append(" and sf.stagedFileStatus != :")
            .append(STATUS)
            .append(")");

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH_DIR_UPLOAD = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) =:")
            .append(PROCESSOR_ID)
            .append(" and sf.filePath =:")
            .append(FILE_PATH)
            .append(" and sf.stagedFileStatus IN (:")
            .append(StagedFileDAO.STATUS)
            .append("))");

    StringBuilder GET_STAGED_FILE_BY_PRCSR_GUID_AND_FILE_PATH = new StringBuilder().append("select sf from StagedFile sf")
            .append(" where (sf.processorId) =:")
            .append(PROCESSOR_ID)
            .append(" and sf.filePath =:")
            .append(FILE_PATH)
            .append(" and sf.stagedFileStatus !=:")
            .append(StagedFileDAO.STATUS)
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
            .append(" order by sf.createdDate desc");

}
