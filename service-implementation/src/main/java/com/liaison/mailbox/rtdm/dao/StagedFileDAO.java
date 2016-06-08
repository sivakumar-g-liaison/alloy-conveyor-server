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

import java.util.List;
import java.util.Map;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;

/**
 * The dao class for the STAGED_FILE database table.
 * 
 * @author OFS
 */

public interface StagedFileDAO extends GenericDAO<StagedFile> {

	public static final String FILE_NAME = "fileName";

	public static final String GUID = "file_guid";

	public static final String CURRENT_TIME = "current_time";

	public static final String STATUS = "status";
	public static final String TYPE = "type";

	public static final String PROCESSOR_ID = "processor_id";
	public static final String FILE_PATH = "filePath";
	public static final String EXEC_STATUS = "exec_status";

	/**
	 * Method to retrieve the list of all staged files of given mailbox ids
	 *
	 * @param mailboxIds
	 * @return list of stagedFiles
	 */
	public List <StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);
	public List <StagedFile> findStagedFilesOfMailboxesBasedonGUID(List<String> mailboxIds, String guid);
	public int getStagedFilesCountByName(List<String> mailboxIds, String fileName,String status);
	public void persistStagedFile(WorkTicket workticket, String processorId, String processorType);
	public StagedFile findStagedFilesByProcessorId(String processorId, String targetLocation, String fileName);
	public List <StagedFile> findStagedFilesByProcessorId(String processorId);

}
