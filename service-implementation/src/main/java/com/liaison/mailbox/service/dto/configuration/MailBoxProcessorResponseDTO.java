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

import com.liaison.mailbox.jpa.model.Processor;

/**
 * Data Transfer Object for processor details in MailBox.
 * 
 * @author veerasamyn
 */
public class MailBoxProcessorResponseDTO {

	private String guid;
	private String name;
	private String type;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void copyFromEntity(Processor processor) {

		this.setGuid(processor.getPguid());
		this.setType(processor.getProcessorType());
		// this.setName();// TODO change it as processor name
	}
}
