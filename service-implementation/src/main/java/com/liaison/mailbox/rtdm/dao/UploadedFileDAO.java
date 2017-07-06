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
	
	String FILE_NAME = "file_Name";
	String FILE_SIZE = "file_Size";
	String UPLOADED_COMMENT = "uploaded_comment";
	String TTL = "ttl";
	String STATUS = "status";
	String USER_ID = "userid";
	String UPLOAD_DATE ="upload_date";
	String PROFILE_NAME ="profile_name";
	
	int getUploadedFilesCountByUserId(String loginId, String fileName, String status);
	List<UploadedFile> findUploadedFiles(String loginId, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);
}
