/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.dao.InboundFileDAO;

/**
 * The persistent class for the INBOUND_FILE database table.
 *
 */
@Entity
@Table(name = "INBOUND_FILE")
@NamedQueries({
    @NamedQuery(name = InboundFileDAO.GET_INPROGRESS_TRIGGER_FILE,
            query = "SELECT inboundFile FROM InboundFile inboundFile"
                    + " WHERE inboundFile.filePath =:" + InboundFileDAO.FILE_PATH
                    + " AND inboundFile.status = 'ACTIVE'"
                    + " AND inboundFile.triggerFile = 1"
                    + " AND inboundFile.processDc =:" + MailBoxConstants.PROCESS_DC
                    + " AND inboundFile.processorId =:" + InboundFileDAO.PROCESSOR_GUID
                    + " AND inboundFile.fileName LIKE '%.INP'")
})
public class InboundFile implements Identifiable {

    private static final long serialVersionUID = 1L;

    private String pguid;
    private String fileName;
    private String fileSize;
    private String filePath;
    private Timestamp fileLasModifiedTime;
    private String processorId;
    private String fs2Uri;
    private String status;
    private String parentGlobalProcessId;
    private Integer triggerFile;
    private String originatingDc;
    private String processDc;
    private Timestamp createdDate;
    private String createdBy;
    private Timestamp modifiedDate;
    private String modifiedBy;

    @Id
    @Column(unique = true, nullable = false, length = 32)
    public String getPguid() {
        return pguid;
    }

    public void setPguid(String pguid) {
        this.pguid = pguid;
    }

    @Column(name = "FILE_NAME", nullable = false, length = 512)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "FILE_SIZE", nullable = false, length = 20)
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Column(name = "FILE_PATH", nullable = false, length = 512)
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Column(name = "FILE_LAST_MODIFIED_TIME")
    public Timestamp getFileLasModifiedTime() {
        return fileLasModifiedTime;
    }

    public void setFileLasModifiedTime(Timestamp fileLasModifiedTime) {
        this.fileLasModifiedTime = fileLasModifiedTime;
    }

    @Column(name = "PROCESSOR_ID", nullable = false, length = 32)
    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    @Column(name = "FS2_URI", nullable = false, length = 512)
    public String getFs2Uri() {
        return fs2Uri;
    }

    public void setFs2Uri(String fs2Uri) {
        this.fs2Uri = fs2Uri;
    }

    @Column(name = "STATUS", nullable = false, length = 16)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "PARENT_GLOBAL_PROCESS_GUID", length = 32)
    public String getParentGlobalProcessId() {
        return parentGlobalProcessId;
    }

    public void setParentGlobalProcessId(String parentGlobalProcessId) {
        this.parentGlobalProcessId = parentGlobalProcessId;
    }

    @Column(name = "TRIGGER_FILE", nullable = false, length = 1)
    public Integer getTriggerFile() {
        return triggerFile;
    }

    public void setTriggerFile(Integer triggerFile) {
        this.triggerFile = triggerFile;
    }

    @Column(name = "ORIGINATING_DC", nullable = false, length = 5)
    public String getOriginatingDc() {
        return originatingDc;
    }

    public void setOriginatingDc(String originatingDc) {
        this.originatingDc = originatingDc;
    }

    @Column(name = "PROCESS_DC", nullable = false, length = 5)
    public String getProcessDc() {
        return processDc;
    }

    public void setProcessDc(String processDc) {
        this.processDc = processDc;
    }

    @Column(name = "CREATED_DATE", nullable = false)
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "CREATED_BY", nullable = false, length = 128)
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "MODIFIED_DATE")
    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Column(name = "MODIFIED_BY", length = 128)
    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    @Transient
	public Object getPrimaryKey() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transient
	public Class getEntityClass() {
        return this.getClass();
    }
}
