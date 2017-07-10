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

import java.util.List;
import java.util.Map;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;

/**
 * The dao class for the UPLOADED_FILE database table.
 *
 */
public interface UploadedFileDAO extends GenericDAO<UploadedFile> {
	
	String TTL = "ttl";
	String USER_ID = "userid";
	String CURRENT_TIME = "current_time";
	String FILE_NAME = "fileName";
	
	int getUploadedFilesCountByUserId(String loginId, String fileName);
	List<UploadedFile> findUploadedFiles(String loginId, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);
}
