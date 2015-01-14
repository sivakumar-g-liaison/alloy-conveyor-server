package com.liaison.mailbox.service.dto.configuration.response;

import java.util.List;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.StagedFileDTO;

public class GetStagedFilesResponseDTO extends CommonResponseDTO {

	private List<StagedFileDTO> stagedFiles;

	public List<StagedFileDTO> getStagedFiles() {
		return stagedFiles;
	}

	public void setStagedFiles(List<StagedFileDTO> stagedFiles) {
		this.stagedFiles = stagedFiles;
	}
	
}
