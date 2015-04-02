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

import java.io.Serializable;

import com.liaison.mailbox.dtdm.model.MailBoxProperty;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.validation.Mandatory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Data Transfer Object for MailBox & Processor Property.
 * 
 * @author veerasamyn
 */
@ApiModel(value = "property")
public class PropertyDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty( value = "Property name", required = true)
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
	
    /**
     *  Copies all the data from DTO to MailBoxProperty. 
     * 
     * @param entity
     *        The MailBoxProperty Entity
     * @param isMailBox
     */
	public void copyToEntity(Object entity, boolean isMailBox) {

		if (isMailBox) {

			MailBoxProperty prop = (MailBoxProperty) entity;
			prop.setPguid(MailBoxUtil.getGUID());
			prop.setMbxPropName(this.getName());
			prop.setMbxPropValue(this.getValue());

		} else {

			ProcessorProperty prop = (ProcessorProperty) entity;
			prop.setPguid(MailBoxUtil.getGUID());
			prop.setProcsrPropName(this.getName());
			prop.setProcsrPropValue(this.getValue());
		}

	}
    
	/**
	 * Copies all the data from MailBoxProperty to dto.
	 * 
	 * @param entity
	 *        The MailBoxProperty Entity
	 * @param isMailBox
	 */
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
