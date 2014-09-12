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

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * Data Transfer Object that implements fields required for payload ticket request.
 * 
 * @author ofs
 */
@JsonRootName("payloadTicketRequest")
public class PayloadTicketRequestDTO {

	private String mailboxId;
	private String spectrumUrl;
	private String targetFileName;
	private String payload;
	
	private PayloadTicketRequestDTO() {
		super();
	}
	
	public PayloadTicketRequestDTO(String mailboxId, String spectrumUrl, String targetFileName, String payload) {
		this.mailboxId = mailboxId;
		this.spectrumUrl = spectrumUrl;
		this.targetFileName = targetFileName;
		this.payload = payload;
	}
	
	public String getMailboxId() {
		return mailboxId;
	}

	public void setMailboxId(String mailboxId) {
		this.mailboxId = mailboxId;
	}

	public String getSpectrumUrl() {
		return spectrumUrl;
	}

	public void setSpectrumUrl(String spectrumUrl) {
		this.spectrumUrl = spectrumUrl;
	}

	public String getTargetFileName() {
		return targetFileName;
	}

	public void setTargetFileName(String targetFileName) {
		this.targetFileName = targetFileName;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	
}

