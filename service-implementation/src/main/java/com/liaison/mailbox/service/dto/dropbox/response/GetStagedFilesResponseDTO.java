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
	private String hitCounter;
	private int totalItems;

	public List<StagedFileDTO> getStagedFiles() {
		return stagedFiles;
	}

	public void setStagedFiles(List<StagedFileDTO> stagedFiles) {
		this.stagedFiles = stagedFiles;
	}

	public String getHitCounter() {
		return hitCounter;
	}

	public void setHitCounter(String hitCounter) {
		this.hitCounter = hitCounter;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}
}
