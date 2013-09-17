/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.request;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Data Transfer Object that implements fields required for mailbox configuration request.
 * 
 * @author veerasamyn
 */
@JsonRootName("addMailBoxRequest")
public class AddMailboxRequestDTO {

	private MailBoxDTO mailBox;

	public MailBoxDTO getMailBox() {
		return mailBox;
	}

	public void setMailBox(MailBoxDTO mailBox) {
		this.mailBox = mailBox;
	}

	public void copyToEntity(MailBox entity) throws MailBoxConfigurationServicesException {
		this.getMailBox().copyToEntity(entity);
	}

}
