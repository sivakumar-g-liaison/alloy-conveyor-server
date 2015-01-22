package com.liaison.mailbox.service.dto.dropbox;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("meta")
public class StagedFileMetaDataDTO {
	
	public StagedFileMetaDataDTO() {
		
	}
	
	public StagedFileMetaDataDTO(String size) {
		this.setSize(size);
	}
	
	private String size;

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

}
