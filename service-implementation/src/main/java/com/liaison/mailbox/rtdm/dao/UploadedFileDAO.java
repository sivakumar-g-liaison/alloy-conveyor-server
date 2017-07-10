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
 */
public interface UploadedFileDAO extends GenericDAO<UploadedFile> {

    String TTL = "ttl";
    String FILE_NAME = "fileName";
    String USER_ID = "userId";
    String EXPIRY_DATE = "expiryDate";
    String SORT_DIR_DESC = "DESC";

    /**
     * get the count of uploaded files
     * @param loginId user id
     * @param fileName file name filtering
     * @return count
     */
    int getUploadedFilesCountByUserId(String loginId, String fileName);

    /**
     * fetch uploaded files of the given user id
     *
     * @param loginId user id
     * @param searchFilter filter details
     * @param pageOffsetDetails pagination details
     * @return list of uploaded files
     */
    List<UploadedFile> fetchUploadedFiles(String loginId, GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails);

}
