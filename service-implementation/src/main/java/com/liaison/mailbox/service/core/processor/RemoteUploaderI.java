/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import com.liaison.mailbox.dtdm.dao.MailboxDTDMDAO;

import java.io.IOException;

/**
 * API only applicable for RemoteUploader
 *
 * Created by VNagarajan on 6/3/2016.
 */
public interface RemoteUploaderI {

    /**
     * Upload a file which isn't require profile invocation
     *
     * @param fileName file name to be uploaded
     */
    void doDirectUpload(String fileName, String folderPath, String globalProcessId);

}