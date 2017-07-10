/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import static com.liaison.mailbox.MailBoxConstants.TTL_UNIT_SECONDS;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.service.dto.dropbox.UploadedFileDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * The persistent class for the UPLOADED_FILE database table.
 *
 */
@Entity
@Table(name = "UPLOADED_FILE")
public class UploadedFile  implements Identifiable {

    private static final long serialVersionUID = 1L;
    
    private String pguid;
    private String userId;
    private String fileName;
    private Long fileSize;
    private String comment;
    private String transferProfile;
    private Timestamp uploadDate;
    private String status;
    private Timestamp expiryDate;
    
    public UploadedFile() {
    }
    
    @Id
    @Column(unique = true, nullable = false, length = 36)
    public String getPguid() {
        return pguid;
    }

    public void setPguid(String pguid) {
        this.pguid = pguid;
    }

    @Column(name = "USERID", length = 128)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Column(name = "FILE_NAME", length = 512)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "FILE_SIZE", length = 256)
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Column(name = "UPLOADED_COMMENT", length = 512)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Column(name = "PROFILE_NAME", nullable = false, length = 512)
    public String getTransferProfile() {
        return transferProfile;
    }

    public void setTransferProfile(String transferProfile) {
        this.transferProfile = transferProfile;
    }

    @Column(name = "UPLOAD_DATE")
    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Column(name = "STATUS", nullable = false, length = 16)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    @Column(name = "EXPIRY_DATE", nullable = false)
    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    @Transient
    public Object getPrimaryKey() {
        return getPguid();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transient
    public Class getEntityClass() {
        return this.getClass();
    }
    
    /**
     * Copies required data from DTO to Entity
     *
     * @param uploadedFileDto The UploadedFileDTO
     * @param isCreate
     */
    public void copyFromDto(UploadedFileDTO uploadedFileDto, boolean isCreate) {
        
        Timestamp timestamp = MailBoxUtil.getTimestamp();
        if (isCreate) {
            this.setPguid(MailBoxUtil.getGUID());
            this.setUploadDate(timestamp);
        } else {
            this.setPguid(uploadedFileDto.getId());            
            this.setUploadDate(new Timestamp(uploadedFileDto.getUploadDate().getTime()));
        }
        
        this.setComment(uploadedFileDto.getComment());
        this.setFileName(uploadedFileDto.getFileName());
        this.setFileSize(uploadedFileDto.getFileSize());
        this.setStatus(uploadedFileDto.getStatus());
        this.setTransferProfile(uploadedFileDto.getTransferProfile());
        Date date = MailBoxUtil.calculateExpires(Integer.parseInt(uploadedFileDto.getTtl()), TTL_UNIT_SECONDS);        
        this.setExpiryDate(new Timestamp(date.getTime()));
        this.setUserId(uploadedFileDto.getUserId());
    }    
}
