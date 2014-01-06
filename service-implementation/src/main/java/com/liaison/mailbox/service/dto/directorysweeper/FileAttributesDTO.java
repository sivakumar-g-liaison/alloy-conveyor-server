package com.liaison.mailbox.service.dto.directorysweeper;

public class FileAttributesDTO {

	private String guid;
	private String filename;
	private Long size;
	private String folderdername;
	private String timestamp;
	private String fs2Path;
	private String pipeLineID;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getFs2Path() {
		return fs2Path;
	}

	public void setFs2Path(String fs2Path) {
		this.fs2Path = fs2Path;
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

	public String getPipeLineID() {
		return pipeLineID;
	}

	public void setPipeLineID(String pipeLineID) {
		this.pipeLineID = pipeLineID;
	}

}
