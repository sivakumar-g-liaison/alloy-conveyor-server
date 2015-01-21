/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;

public class StagedFileDTO {
	
	private String fileName;
	private String filePguid;
	private String filePath;
	private String fileSize;
	private String mailboxGuid;
	private String spectrumUri;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePguid() {
		return filePguid;
	}
	public void setFilePguid(String filePguid) {
		this.filePguid = filePguid;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
}
