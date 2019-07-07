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

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 * Data Transfer Object used for sending the staged file responses.
 * 
 * @author OFS
 */
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
