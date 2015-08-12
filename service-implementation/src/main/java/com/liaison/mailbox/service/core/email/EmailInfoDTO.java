/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.email;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.dtdm.model.Processor;

/**
 * @author OFS
 *
 */
public class EmailInfoDTO {

    private static final Logger LOGGER = LogManager.getLogger(EmailInfoDTO.class);

    private String emailBody;
	private String mailboxName;
	private String mailboxId;
	private String processorName;
	private String subject;
	private String type;
	private List<String> toEmailAddrList;
	private boolean isSuccess;

	public String getMailboxName() {
		return mailboxName;
	}
	public void setMailboxName(String mailboxName) {
		this.mailboxName = mailboxName;
	}
	public String getMailboxId() {
		return mailboxId;
	}
	public void setMailboxId(String mailboxId) {
		this.mailboxId = mailboxId;
	}
	public String getProcessorName() {
		return processorName;
	}
	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getEmailBody() {
		return emailBody;
	}
	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getToEmailAddrList() {
		return toEmailAddrList;
	}
	public void setToEmailAddrList(List<String> toEmailAddrList) {
		this.toEmailAddrList = toEmailAddrList;
	}
	public boolean isSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public void copyToDTO(Processor processor, List<String> emailAddress, String emailSubject, String failureReason, boolean isSuccess) {

	    LOGGER.debug("Ready to construct Email Helper DTO for the mail to be sent for processor execution failure");
        this.setMailboxId(null != processor ? processor.getMailbox().getPguid() : null);
        this.setMailboxName(null != processor ? processor.getMailbox().getMbxName() : null);
        this.setProcessorName(null !=  processor ? processor.getProcsrName() : null);
        this.setType("HTML");
        this.setEmailBody(failureReason);
        this.setToEmailAddrList(emailAddress);
        this.setSubject(emailSubject);
        this.setSuccess(isSuccess);
        LOGGER.debug("Email Helper DTO Constructed for the mail to be sent for processor execution failure");
	}

}
