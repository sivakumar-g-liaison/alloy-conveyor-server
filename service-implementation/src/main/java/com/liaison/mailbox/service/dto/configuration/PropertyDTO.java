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

import com.liaison.mailbox.jpa.model.MailBoxProperty;
import com.liaison.mailbox.jpa.model.ProcessorProperty;
import com.liaison.mailbox.service.util.MailBoxUtility;
import com.liaison.mailbox.service.validation.Mandatory;

/**
 * Data Transfer Object for MailBox & Processor Property.
 * 
 * @author veerasamyn
 */
public class PropertyDTO {

	private String name;
	private String value;

	@Mandatory(errorMessage = "Property name is mandatory.")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Mandatory(errorMessage = "Property value is mandatory.")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void copyToEntity(Object entity, boolean isMailBox) {

		if (isMailBox) {

			MailBoxProperty prop = (MailBoxProperty) entity;
			prop.setPguid(MailBoxUtility.getGUID());
			prop.setMbxPropName(this.getName());
			prop.setMbxPropValue(this.getValue());

		} else {

			ProcessorProperty prop = (ProcessorProperty) entity;
			prop.setPguid(MailBoxUtility.getGUID());
			prop.setProcsrPropName(this.getName());
			prop.setProcsrPropValue(this.getValue());
		}

	}

	public void copyFromEntity(Object entity, boolean isMailBox) {

		if (isMailBox) {

			MailBoxProperty prop = (MailBoxProperty) entity;
			this.setName(prop.getMbxPropName());
			this.setValue(prop.getMbxPropValue());
		} else {

			ProcessorProperty prop = (ProcessorProperty) entity;
			this.setName(prop.getProcsrPropName());
			this.setValue(prop.getProcsrPropValue());
		}
	}

}
