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

import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;


/**
 * Data Transfer Object that implements fields required for mailbox
 * configuration request. 
 *
 * @author veerasamyn
 */
public class AddMailboxRequestDTO {

	private MailBoxDTO mailbox;

	public MailBoxDTO getMailbox() {
		return mailbox;
	}
	
	public void setMailbox(MailBoxDTO mailbox) {
		this.mailbox = mailbox;
	}
	
	public void copyToEntity(MailBox entity) {
		this.getMailbox().copyToEntity(entity);

	}

}
