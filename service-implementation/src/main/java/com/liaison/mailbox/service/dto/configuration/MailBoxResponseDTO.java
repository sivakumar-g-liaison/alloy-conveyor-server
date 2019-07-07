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

import java.io.Serializable;

import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * The response DTO for mailbox CRUD operations.
 * 
 * @author OFS
 */
public class MailBoxResponseDTO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String guid;

	public MailBoxResponseDTO() { // Added for Deserialization
	}

	public MailBoxResponseDTO(String guid) {
		this.guid = guid;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * Copies the guid from utility to mailbox.
	 * 
	 * @param mailBox
	 */
	public void copyToEntity(MailBox mailBox) {
		mailBox.setPguid(MailBoxUtil.getGUID());
	}

	/**
	 * Copies the pguid from mailbox to dto.
	 * 
	 * @param mailBox
	 */
	public void copyFromEntity(MailBox mailBox) {
		this.setGuid(mailBox.getPguid());
	}
}
