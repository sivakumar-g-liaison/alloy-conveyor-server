package com.liaison.mailbox.service.internal.helper.dto;

import java.io.InputStream;

public class FileTransferHelperDTO {
    
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
