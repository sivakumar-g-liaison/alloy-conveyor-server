/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox;

import java.io.InputStream;

/**
 * Data Transfer Object that contains the meta data about the file.
 * 
 * @author OFS
 */
public class FileTransferMetaDTO {
    
    String fileName;
    String loginId;
    String transferProfileId;
    String transferProfileName;
    String tenancyKey;
    InputStream fileContent;
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getLoginId() {
        return loginId;
    }
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
    public String getTransferProfileId() {
        return transferProfileId;
    }
    public void setTransferProfileId(String transferProfileId) {
        this.transferProfileId = transferProfileId;
    }
    public String getTransferProfileName() {
        return transferProfileName;
    }
    public void setTransferProfileName(String transferProfileName) {
        this.transferProfileName = transferProfileName;
    }
    public String getTenancyKey() {
        return tenancyKey;
    }
    public void setTenancyKey(String tenancyKey) {
        this.tenancyKey = tenancyKey;
    }
    public InputStream getFileContent() {
        return fileContent;
    }
    public void setFileContent(InputStream fileContent) {
        this.fileContent = fileContent;
    }
    
}
