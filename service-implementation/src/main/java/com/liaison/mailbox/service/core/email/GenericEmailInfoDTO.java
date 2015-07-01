package com.liaison.mailbox.service.core.email;

import java.util.List;

public class GenericEmailInfoDTO {

	private String mailboxName;
	private String mailboxId;
	private String processorName;
	private String subject;
	private String emailBody;
	private String type;
	private List<String> toEmailAddrList;

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
}
