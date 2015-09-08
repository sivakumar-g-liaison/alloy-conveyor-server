/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.dropbox.request;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.dropbox.StagedFileDTO;

/**
 * Data Transfer Object that contains the fields required for staged file request.
 * 
 * @author OFS
 */
@JsonRootName("addStagedFileRequest")
public class StagePayloadRequestDTO {

	private StagedFileDTO stagedFile;
	
	public StagedFileDTO getStagedFile() {
		return stagedFile;
	}

	public void setStagedFile(StagedFileDTO stagedFile) {
		this.stagedFile = stagedFile;
	}
}
