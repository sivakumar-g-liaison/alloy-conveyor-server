/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto;

import com.liaison.mailbox.enums.Messages;

/**
 * Class which contains response message and status.
 * 
 * @author veerasamyn
 */
public class ResponseDTO {

	private String message;
	private String status;

	public ResponseDTO() {
	}

	public ResponseDTO(Messages message, String key, Messages status) {
		this.message = String.format(message.value(), key);
		this.status = status.value();
	}

	public ResponseDTO(Messages message, String key, Messages status, String additionalMessage) {
		this.message = String.format(message.value(), key) + additionalMessage;
		this.status = status.value();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(Messages message, String key) {
		this.message = String.format(message.value(), key);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setAdditionalMessage(String addMessage) {
		message = message.concat(addMessage);
	}

}
