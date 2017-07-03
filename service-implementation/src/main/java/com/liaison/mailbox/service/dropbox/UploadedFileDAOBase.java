/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dropbox;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.dao.UploadedFileDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;

/**
 * This will fetch the uploaded file details. 
 *
 */
public class UploadedFileDAOBase extends GenericDAOBase<UploadedFile> implements UploadedFileDAO, MailboxRTDMDAO {
    
    public UploadedFileDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }
    
}
