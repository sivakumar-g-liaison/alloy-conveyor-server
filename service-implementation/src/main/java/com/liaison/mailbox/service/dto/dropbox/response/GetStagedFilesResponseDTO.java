/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox.response;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;

/**
 * Data Transfer Object used for retrieving the staged files.
 *
 * @author OFS
 */
@JsonRootName("getStagedFilesResponse")
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
