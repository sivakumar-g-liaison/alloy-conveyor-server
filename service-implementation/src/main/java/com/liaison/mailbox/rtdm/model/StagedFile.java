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
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

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
	private String fileMetaData;
	private String stagedFileStatus;
	private Timestamp timeToLive;
	
	
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
	
	@Column(name = "TIMETOLIVE", nullable =false)
	public Timestamp getTTLStatus() {
		return timeToLive;
	}

	public void setTTLStatus(Timestamp timeToLive) {
		this.timeToLive = timeToLive;
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
	 * Copies all data from entity to DTO except PGUID.
	 * 
	 * @param stagedFileDTO
	 *            The StagedFile DTO
	 * @param copyAll - boolean           
	 * @throws IOException 
	 * @throws JAXBException 
	 */
	public void copyToDto(StagedFileDTO stagedFileDto, boolean copyAll) throws IOException, JAXBException {
		
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
	}
	
	/**
	 * Copies required data from DTO to Entity
	 * 
	 * @param stagedFileDTO
	 * 			The StagedFileDTO
	 * @param boolean isCreate
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public void copyFromDto(StagedFileDTO stagedFileDto, boolean isCreate) throws JsonGenerationException, JsonMappingException, JAXBException, IOException {		
		
		if(isCreate){
			this.setPguid(MailBoxUtil.getGUID()); 
		}
		this.setFileName(stagedFileDto.getName());
		this.setFilePath(stagedFileDto.getPath());
		this.setFileSize(stagedFileDto.getFileSize());
		this.setMailboxId(stagedFileDto.getMailboxGuid());
		this.setSpectrumUri(stagedFileDto.getSpectrumUri());
		this.setFileMetaData(stagedFileDto.getMeta());
	}
}