/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.configuration;

import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author praveenu
 * 
 */
public class MailBoxResponseDTO {

	private String guid;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public void copyToEntity(MailBox mailBox) {
		mailBox.setPguid(MailBoxUtility.getGUID());
	}

	public void copyFromEntity(MailBox mailBox) {
		this.setGuid(mailBox.getPguid());
	}
}
