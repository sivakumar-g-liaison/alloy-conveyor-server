package com.liaison.mailbox.service.dto.dropbox;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("meta")
public class StagedFileMetaDataDTO {
	
	private String size;
	private String from;
	private String comment;
	
	public StagedFileMetaDataDTO() {
		
	}
	
	public StagedFileMetaDataDTO(String size, String from, String comment) {
		this.setSize(size);
		this.setFrom(from);
		this.setComment(comment);
	}
	

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
