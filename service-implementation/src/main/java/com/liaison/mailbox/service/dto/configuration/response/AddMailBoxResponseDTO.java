/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.MailBoxResponseDTO;

/**
 * Data Transfer Object uses for sending Add MailBox Responses.
 * 
 * @author veerasamyn
 */
@JsonRootName("addMailBoxResponse")
public class AddMailBoxResponseDTO extends CommonResponseDTO{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;	
	private MailBoxResponseDTO mailBox;

	public MailBoxResponseDTO getMailBox() {
		return mailBox;
	}

	public void setMailBox(MailBoxResponseDTO mailBox) {
		this.mailBox = mailBox;
	}

	
}
