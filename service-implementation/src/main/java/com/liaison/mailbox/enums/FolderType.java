/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.enums;

import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
 * 
 */
public enum FolderType {

	PAYLOAD_LOCATION("PAYLOAD_LOCATION"),
	RESPONSE_LOCATION("RESPONSE_LOCATION"),
	FILE_WRITE_LOCATION("FILE_WRITE_LOCATION");

	private final String code;

	private FolderType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}

	public String getCode() {
		return code;
	}
    
	/**
	 * This  method will retrieve the FolderType by given Folder type.
	 * 
	 * @param code 
	 *        The Folder type
	 * @return FolderType
	 */
	public static FolderType findByCode(String code) {

		FolderType found = null;
		for (FolderType value : FolderType.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}
    
	/**
	 * This  method will retrieve the FolderType by given folderType from FolderDTO.
	 * 
	 * @param name 
	 *        The folder type
	 * @return FolderType
	 */
	public static FolderType findByName(String name) {

		FolderType found = null;
		for (FolderType value : FolderType.values()) {

			if (!MailBoxUtil.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
