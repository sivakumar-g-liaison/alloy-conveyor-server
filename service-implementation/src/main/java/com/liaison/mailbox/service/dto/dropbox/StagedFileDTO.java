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
	private String mailboxGuid;
	private String spectrumUri;
	private String meta;
	private String status;
	private String expirationTime;

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
		this.setExpirationTime(workTicket.getHeader(MailBoxConstants.FS2_OPTIONS_TTL));
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
}
