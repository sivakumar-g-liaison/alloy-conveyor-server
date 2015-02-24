package com.liaison.mailbox.service.dto.configuration.processor.properties;

public class ValidationRulesDTO {
	
	private String pattern;
	private String minLength;
	private String maxLength;
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getMinLength() {
		return minLength;
	}
	public void setMinLength(String minLength) {
		this.minLength = minLength;
	}
	public String getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}
	
	

}
