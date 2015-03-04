/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox;

public class StagedFileDTO {

	private String name;
	private String id;
	private String path;
	private String fileSize;
	private String mailboxGuid;
	private String spectrumUri;
	private String meta;

	public StagedFileDTO() {

	}

	public StagedFileDTO(String fileName, String filePguid, String filePath, String fileSize, String mailboxGuid,
			String spectrumUri, String meta) {
		this.setName(fileName);
		this.setId(filePguid);
		this.setPath(filePath);
		this.setFileSize(fileSize);
		this.setMailboxGuid(mailboxGuid);
		this.setSpectrumUri(spectrumUri);
		this.setMeta(meta);
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
}
