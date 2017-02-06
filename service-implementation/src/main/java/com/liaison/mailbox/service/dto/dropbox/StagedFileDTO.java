/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox;

import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.rtdm.model.StagedFile;

/**
 * Data Transfer Object that contains the staged file details.
 *
 * @author OFS
 */
public class StagedFileDTO {

	private String name;
	private String id;
	private String path;
	private String fileSize;
	private String globalProcessId;
	private String mailboxGuid;
	private String processorId;
	private String processorType;
	private String spectrumUri;
	private String meta;
	private String status;
	private String expirationTime;
	private String createdDate;
	private String modifiedDate;

	public StagedFileDTO() {
	}

	public StagedFileDTO(WorkTicket workTicket) {
		this.setName(workTicket.getFileName());
		this.setPath(null != workTicket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_PATH)
		        ? workTicket.getAdditionalContext().get(MailBoxConstants.KEY_FILE_PATH).toString() : null);
		this.setFileSize(workTicket.getPayloadSize().toString());
		this.setMailboxGuid((null != workTicket.getAdditionalContext().get(MailBoxConstants.KEY_MAILBOX_ID))
		        ? workTicket.getAdditionalContext().get(MailBoxConstants.KEY_MAILBOX_ID).toString() : null);
		this.setSpectrumUri(workTicket.getPayloadURI());
		this.setMeta(workTicket.getHeader(MailBoxConstants.UPLOAD_META));
		this.setStatus(EntityStatus.ACTIVE.value());
		this.setGlobalProcessId(workTicket.getGlobalProcessId());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getMailboxGuid() {
		return mailboxGuid;
	}

	public void setMailboxGuid(String mailboxGuid) {
		this.mailboxGuid = mailboxGuid;
	}

	public String getSpectrumUri() {
		return spectrumUri;
	}

	public void setSpectrumUri(String spectrumUri) {
		this.spectrumUri = spectrumUri;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public String getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(String expirationTime) {
		this.expirationTime = expirationTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	public String getProcessorType() {
		return processorType;
	}

	public void setProcessorType(String processorType) {
		this.processorType = processorType;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getGlobalProcessId() {
		return globalProcessId;
	}

	public void setGlobalProcessId(String globalProcessId) {
		this.globalProcessId = globalProcessId;
	}

	/**
	 * Copies the file details from file to dto.
	 * 
	 * @param file
	 */
	public void copyFromEntity(StagedFile file) {

		this.setCreatedDate(file.getCreatedDate().toString());
		this.setGlobalProcessId(file.getGlobalProcessId());
		this.setMailboxGuid(file.getMailboxId());
		this.setName(file.getFileName());
		this.setPath(file.getFilePath());
		this.setMeta(file.getFileMetaData());
		this.setProcessorId(file.getProcessorId());
		this.setFileSize(file.getFileSize());
		this.setStatus(file.getStagedFileStatus());
		this.setModifiedDate(file.getModifiedDate().toString());
		this.setProcessorType(file.getProcessorType());
		this.setSpectrumUri(file.getSpectrumUri());
		this.setExpirationTime(file.getExpirationTime().toString());
		this.setId(file.getPguid());
	}

}
