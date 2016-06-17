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

    String FILE_NAME = "fileName";
    String GUID = "file_guid";
    String CURRENT_TIME = "current_time";
    String STATUS = "status";
    String TYPE = "type";
    String PROCESSOR_ID = "processor_id";
    String GLOBAL_PROCESS_ID = "gpid";
    String FILE_PATH = "filePath";
    String EXEC_STATUS = "exec_status";

	/**
	 * Method to retrieve the list of all staged files of given mailbox ids
	 *
	 * @param mailboxIds
	 * @return list of stagedFiles
	 */
	List <StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);
	List <StagedFile> findStagedFilesOfMailboxesBasedonGUID(List<String> mailboxIds, String guid);
	int getStagedFilesCountByName(List<String> mailboxIds, String fileName,String status);
	void persistStagedFile(WorkTicket workticket, String processorId, String processorType);
	StagedFile findStagedFilesByProcessorId(String processorId, String targetLocation, String fileName);
	List <StagedFile> findStagedFilesByProcessorId(String processorId);

    /**
     * list staged files by processor id
     * @param processorId procesor id
     * @return list of staged files
     */
    List<StagedFile> findStagedFilesForUploader(String processorId);

    /**
     * list staged files by processor id
     * @param processorId procesor id
     * @return list of staged files
     */
    List<StagedFile> findStagedFilesForUploader(String processorId, String filePath);

    /**
     * get staged file by gpid
     * @param gpid global process id
     * @return staged file
     */
    StagedFile findStagedFile(String gpid);

}
