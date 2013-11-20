package com.liaison.mailbox.service.dto.directorysweeper;

public class FileAttributesDTO {

	private String guid;
	private String filename;
	private Long size;
	private String folderdername;
	private String timestamp;
	private String filePath;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getFolderdername() {
		return folderdername;
	}

	public void setFolderdername(String folderdername) {
		this.folderdername = folderdername;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
