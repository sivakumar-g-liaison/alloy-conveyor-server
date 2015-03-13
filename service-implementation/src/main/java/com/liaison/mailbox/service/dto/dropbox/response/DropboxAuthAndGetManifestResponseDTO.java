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

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.ResponseDTO;

/**
 *
 * @author OFS
 *
 */
@JsonRootName("authAndGetACLResponse")
public class DropboxAuthAndGetManifestResponseDTO extends CommonResponseDTO {

	public DropboxAuthAndGetManifestResponseDTO() {
	}

	public DropboxAuthAndGetManifestResponseDTO(Messages message, Messages status) {
		this.setResponse(new ResponseDTO(message,status,""));
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;




}
