package com.liaison.mailbox.service.dto.dropbox.response;

import com.liaison.mailbox.service.dto.CommonResponseDTO;

public class StagePayloadResponseDTO extends CommonResponseDTO {
	
	private static final long serialVersionUID = 1L;
	private StagedFileResponseDTO stagedFile;
	
	public StagedFileResponseDTO getStagedFile() {
		return stagedFile;
	}
	public void setStagedFile(StagedFileResponseDTO stagedFile) {
		this.stagedFile = stagedFile;
	}
}
