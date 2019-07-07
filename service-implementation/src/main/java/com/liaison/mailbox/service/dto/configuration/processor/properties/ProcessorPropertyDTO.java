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

import java.util.List;

import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Data Transfer Object for properties of the processor.
 * 
 * @author OFS
 */
public class ProcessorPropertyDTO {

	private String name;
	private String displayName;
	private String value;
	private String type;
	private boolean readOnly;
	private boolean mandatory;
	private boolean dynamic;
	private boolean valueProvided;
	private String defaultValue;
	private ValidationRulesDTO validationRules;
	private List<String> options;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public boolean isDynamic() {
		return dynamic;
	}
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	public boolean isValueProvided() {
		return valueProvided;
	}
	public void setValueProvided(boolean valueProvided) {
		this.valueProvided = valueProvided;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public ValidationRulesDTO getValidationRules() {
		return validationRules;
	}
	public void setValidationRules(ValidationRulesDTO validationRules) {
		this.validationRules = validationRules;
	}
	public List<String> getOptions() {
		return options;
	}
	public void setOptions(List<String> options) {
		this.options = options;
	}

	 /**
     *  Copies name and value from property DTO to ProcessorProperty Entity.
     *
     * @param entity
     *        The ProcessorProperty Entity
     */
	public void copyToEntity(Object entity) {

		ProcessorProperty prop = (ProcessorProperty) entity;
		prop.setPguid(MailBoxUtil.getGUID());
		prop.setOriginatingDc(MailBoxUtil.DATACENTER_NAME);
		prop.setProcsrPropName(this.getName());
		prop.setProcsrPropValue(this.getValue());

	}
}
