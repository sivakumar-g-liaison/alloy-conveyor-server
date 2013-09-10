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

import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * Data Transfer Object for MailBox Property.
 * 
 * @author veerasamyn
 */
public class ProcessorPropertyDTO {

	private String name;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void copyToEntity(ProcessorProperty prop) {

		prop.setPguid(MailBoxUtility.getGUID());
		prop.setProcsrPropName(this.getName());
		prop.setProcsrPropValue(this.getValue());
	}

	public void copyFromEntity(Object entity) {

		ProcessorProperty prop = (ProcessorProperty) entity;
		this.setName(prop.getProcsrPropName());
		this.setValue(prop.getProcsrPropValue());
	}

}
