/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.grammer.dto;

import com.liaison.mailbox.jpa.model.MailBoxProperty;

/**
 * Data Transfer Object for MailBox Property.
 *
 * @author veerasamyn
 */
public class MailBoxPropertyDTO {

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

	public void copyToEntity(Object entity) {

		MailBoxProperty prop = (MailBoxProperty) entity;
		prop.setMbxPropName(this.getName());
		prop.setMbxPropValue(this.getValue());

	}

	public void copyFromEntity(Object entity) {

		MailBoxProperty prop = (MailBoxProperty) entity;
		this.setName(prop.getMbxPropName());
		this.setValue(prop.getMbxPropValue());
	}

}
