package com.liaison.mailbox.service.dto.dropbox.response;

import java.util.List;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;

public class GetStagedFilesResponseDTO extends CommonResponseDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<StagedFileDTO> stagedFiles;

	public List<StagedFileDTO> getStagedFiles() {
		return stagedFiles;
	}

	public void setStagedFiles(List<StagedFileDTO> stagedFiles) {
		this.stagedFiles = stagedFiles;
	}
	
}
