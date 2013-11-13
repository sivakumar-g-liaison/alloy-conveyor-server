package com.liaison.mailbox.service.dto;

public class ConfigureJNDIDTO {
	private String initialContextFactory;
	
	private String providerURL;
	
	private String urlPackagePrefixes;

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
}
