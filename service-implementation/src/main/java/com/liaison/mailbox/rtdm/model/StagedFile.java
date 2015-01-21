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

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * The persistent class for the STAGED_FILES database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "STAGED_FILE")
@NamedQuery(name = "StagedFile.findAll", query = "SELECT sf FROM StagedFile sf")
public class StagedFile implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String fileSize;
	private String mailboxId;
	private String fileName;
	private String filePath;
	private String spectrumUri;
	
	
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

	@Column(name = "MAILBOX_ID", nullable = false, length = 32)
	public String getMailboxId() {
		return mailboxId;
	}

	public void setMailboxId(String mailboxId) {
		this.mailboxId = mailboxId;
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

	@Override
	@Transient
	public Object getPrimaryKey() {
		return getPguid();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transient
	public Class getEntityClass() {
		return this.getClass();
	}
	
	/**
	 * Copies all data from DTO to entity except PGUID.
	 * 
	 * @param stagedFile
	 *            The StagedFile Entity
	 * @throws IOException 
	 */
	public void copyToDto(StagedFileDTO stagedFileDto) throws IOException {
		
		stagedFileDto.setMailboxGuid(this.getMailboxId());
		stagedFileDto.setSpectrumUri(this.getSpectrumUri());
		stagedFileDto.setFileName(this.getFileName());
		stagedFileDto.setFilePath(this.getFilePath());
		stagedFileDto.setFileSize(this.getFileSize());
	}
	
	/**
	 * Copies required data from entity to DTO
	 * 
	 * @param stagedFile
	 * 			The StagedFileEntity
	 */
	public void copyFromDto(StagedFileDTO stagedFileDto, boolean isCreate) {		
		if(isCreate){
			this.setPguid(MailBoxUtil.getGUID()); 
		}
		this.setFileName(stagedFileDto.getFileName());
		this.setFilePath(stagedFileDto.getFilePath());
		this.setFileSize(stagedFileDto.getFileSize());
	}
}