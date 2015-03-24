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

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.StagedFile;

/**
 * @author OFS
 * 
 */

public interface StagedFileDAO extends GenericDAO<StagedFile> {
	
	public static final String FILE_NAME = "fileName";
	
	public static final String GUID = "file_guid";
	
	public static final String CURRENT_TIME = "current_time";
	
	/**
	 * Method to retrieve the list of all staged files of given mailbox ids
	 * 
	 * @param mailboxIds
	 * @return list of stagedFiles
	 */
	public List <StagedFile> findStagedFilesOfMailboxes(List<String> mailboxIds, String fileName, int pagingOffset,
			int pagingCount, String sortField, String sortDirection);
	public List <StagedFile> findStagedFilesOfMailboxesBasedonGUID(List<String> mailboxIds, String guid);
	public int getStagedFilesCountByName(List<String> mailboxIds, String fileName);
}
