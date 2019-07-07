/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.processor.properties;

/**
 * Data Transfer Object for the set of processor validation rules.
 * 
 * @author OFS
 */
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
