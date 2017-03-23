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

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.liaison.mailbox.service.util.MailBoxUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.sql.Timestamp;

/**
 * The persistent class for the STAGED_FILES database table.
 *
 *  @author OFS
 */
@Entity
@Table(name = "STAGED_FILE")
@NamedQueries({
        @NamedQuery(name = "StagedFile.findAll", query = "SELECT sf FROM StagedFile sf"),
        @NamedQuery(name = StagedFileDAO.GET_STAGED_FILE_BY_FILE_NAME_AND_FILE_PATH_FOR_FILE_WRITER,
                query = "SELECT sf FROM StagedFile sf"
                        + " WHERE sf.filePath =:" + StagedFileDAO.FILE_PATH
                        + " AND sf.fileName =:" + StagedFileDAO.FILE_NAME
                        + " AND sf.processorType =:" + StagedFileDAO.TYPE
                        + " AND sf.stagedFileStatus <>:" + StagedFileDAO.STATUS
                        + " AND sf.clusterType =:" + MailBoxConstants.CLUSTER_TYPE),
        @NamedQuery(name = StagedFileDAO.FIND_BY_GPID,
                query = "select sf from StagedFile sf"
                        + " WHERE (sf.globalProcessId) =:" + StagedFileDAO.GLOBAL_PROCESS_ID
                        + " AND sf.stagedFileStatus <>:" + StagedFileDAO.STATUS
                        + " AND sf.clusterType =:" + MailBoxConstants.CLUSTER_TYPE)
})
public class StagedFile implements Identifiable {

    private static final long serialVersionUID = 1L;

    private String pguid;
    private String fileSize;
    private String mailboxId;
    private String processorId;
    private String globalProcessId;
    private String processorType;
    private String fileName;
    private String filePath;
    private String spectrumUri;
    private String fileMetaData;
    private Integer failureNotificationCount;
    private String stagedFileStatus;
    private Timestamp expirationTime;
    private Timestamp createdDate;
    private Timestamp modifiedDate;
    private String originatingDc;
    private String clusterType;

    public StagedFile() {
    }

    @Id
    @Column(unique = true, nullable = false, length = 32)
    public String getPguid() {
        return this.pguid;
    }

    public void setPguid(String pguid) {
        this.pguid = pguid;
    }

    @Column(name = "FILE_SIZE", length = 256)
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Column(name = "MAILBOX_GUID", nullable = false, length = 32)
    public String getMailboxId() {
        return mailboxId;
    }

    public void setMailboxId(String mailboxId) {
        this.mailboxId = mailboxId;
    }

    @Column(name = "PROCESSOR_GUID", length = 32)
    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    @Column(name = "GLOBAL_PROCESS_ID", length = 32)
    public String getGlobalProcessId() {
        return globalProcessId;
    }

    public void setGlobalProcessId(String globalProcessId) {
        this.globalProcessId = globalProcessId;
    }

    @Column(name = "PROCESSOR_TYPE", length = 128)
    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    @Column(name = "FILE_NAME", length = 512)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "FILE_PATH", length = 512)
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Column(name = "SPECTRUM_URI", length = 512)
    public String getSpectrumUri() {
        return spectrumUri;
    }

    public void setSpectrumUri(String spectrumUri) {
        this.spectrumUri = spectrumUri;
    }

    @Column(name = "META", length = 512)
    public String getFileMetaData() {
        return fileMetaData;
    }

    public void setFileMetaData(String fileMetaData) {
        this.fileMetaData = fileMetaData;
    }

    @Column(name = "STATUS", nullable = false, length = 16)
    public String getStagedFileStatus() {
        return stagedFileStatus;
    }

    public void setStagedFileStatus(String stagedFileStatus) {
        this.stagedFileStatus = stagedFileStatus;
    }

    @Column(name = "AVAILABLE_UNTIL", nullable = false)
    public Timestamp getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Timestamp timeToLive) {
        this.expirationTime = timeToLive;
    }

    @Column(name = "CREATED_DATE")
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "MODIFIED_DATE")
    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return the failureNotificationCount
     */
    @Column(name = "NOTIFICATION_COUNT", length = 19)
    public Integer getFailureNotificationCount() {
        return failureNotificationCount;
    }

    /**
     * @param failureNotificationCount
     *            the failureNotificationCount to set
     */
    public void setFailureNotificationCount(Integer failureNotificationCount) {
        this.failureNotificationCount = failureNotificationCount;
    }

    @Column(name = "ORIGINATING_DC", length = 16)
    public String getOriginatingDc() {
        return originatingDc;
    }

    public void setOriginatingDc(String originatingDc) {
        this.originatingDc = originatingDc;
    }
    
    @Column(name = "CLUSTER_TYPE", nullable = false, length = 32)
    public String getClusterType() {
        return clusterType;
    }
    
    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
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
     * Copies all data from entity to DTO except PGUID.
     *
     * @param stagedFileDto The StagedFile DTO
     * @param copyAll boolean
     */
    public void copyToDto(StagedFileDTO stagedFileDto, boolean copyAll) {

        // if copyAll is false, mailboxguid and spectrum uri will not be set in StagedFileDTO
        if (copyAll) {
            stagedFileDto.setMailboxGuid(this.getMailboxId());
            stagedFileDto.setSpectrumUri(this.getSpectrumUri());
        }
        stagedFileDto.setId(this.getPguid());
        stagedFileDto.setName(this.getFileName());
        stagedFileDto.setPath(this.getFilePath());
        stagedFileDto.setFileSize(this.getFileSize());
        stagedFileDto.setMeta(this.getFileMetaData());
        stagedFileDto.setStatus(this.getStagedFileStatus());
        stagedFileDto.setExpirationTime(this.getExpirationTime() == null ? "" : this.getExpirationTime().toString());
    }

    /**
     * Copies required data from DTO to Entity
     *
     * @param stagedFileDto The StagedFileDTO
     * @param isCreate
     */
    public void copyFromDto(StagedFileDTO stagedFileDto, boolean isCreate) {

        Timestamp timestamp = MailBoxUtil.getTimestamp();
        if (isCreate) {
            this.setPguid(MailBoxUtil.getGUID());
            this.setCreatedDate(timestamp);
        }
        this.setFileName(stagedFileDto.getName());
        this.setFilePath(stagedFileDto.getPath());
        this.setFileSize(stagedFileDto.getFileSize());
        this.setMailboxId(stagedFileDto.getMailboxGuid());
        this.setSpectrumUri(stagedFileDto.getSpectrumUri());
        this.setFileMetaData(stagedFileDto.getMeta());
        EntityStatus status = MailBoxUtil.isEmpty(stagedFileDto.getStatus())
                ? EntityStatus.ACTIVE
                : EntityStatus.findByCode(stagedFileDto.getStatus());
        this.setStagedFileStatus(status.name());

        //reads TTL from spectrum meta data
        this.setExpirationTime(MailBoxUtil.getExpirationDate(StorageUtilities.getMetaData(stagedFileDto.getSpectrumUri())));

        this.setProcessorId(stagedFileDto.getProcessorId());
        this.setProcessorType(stagedFileDto.getProcessorType());
        this.setModifiedDate(timestamp);
        this.setGlobalProcessId(stagedFileDto.getGlobalProcessId());
        this.setClusterType(MailBoxUtil.CLUSTER_TYPE);
    }

    @Transient
    public String getGPID() {
        return ("NOT AVAILABLE".equals(this.getGlobalProcessId()) ? this.getPguid() : this.getGlobalProcessId());
    }
}