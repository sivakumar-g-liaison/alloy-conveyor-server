package com.liaison.mailbox.service.dto;

import java.util.List;

public class ConfigureJNDIDTO {

	private String initialContextFactory;
	private String providerURL;
	private String urlPackagePrefixes;
	private String message;
	private List<String> messages;
	private String queueName;

	public String getInitialContextFactory() {
		return initialContextFactory;
	}

	public void setInitialContextFactory(String initialContextFactory) {
		this.initialContextFactory = initialContextFactory;
	}

	public String getProviderURL() {
		return providerURL;
	}

	public void setProviderURL(String providerURL) {
		this.providerURL = providerURL;
	}

	public String getUrlPackagePrefixes() {
		return urlPackagePrefixes;
	}

	public void setUrlPackagePrefixes(String urlPackagePrefixes) {
		this.urlPackagePrefixes = urlPackagePrefixes;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
