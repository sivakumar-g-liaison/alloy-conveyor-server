/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox;

import java.text.ParseException;
import java.util.Date;
import com.liaison.mailbox.rtdm.model.UploadedFile;

public class UploadedFileDTO {
	
    private String id;
    private String fileName;
    private Long fileSize;
    private String comment;
    private String transferProfile;
    private Date uploadDate;
    private String status;
    private String ttl;
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTransferProfile() {
        return transferProfile;
    }

    public void setTransferProfile(String transferProfile) {
        this.transferProfile = transferProfile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

	public String getTtl() {
		return ttl;
	}

	public void setTtl(String ttl) {
		this.ttl = ttl;
	}

	public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void copyFromEntity(UploadedFile uploadedFile) throws ParseException {	    
	       
        this.setComment(uploadedFile.getComment());
        this.setFileName(uploadedFile.getFileName());
        this.setFileSize(uploadedFile.getFileSize());
        this.setStatus(uploadedFile.getStatus());
        this.setTransferProfile(uploadedFile.getTransferProfile());        
        this.setUploadDate(new Date(uploadedFile.getUploadDate().getTime()));
        this.setUserId(uploadedFile.getUserId());
        this.setId(uploadedFile.getPguid());
   }
}
