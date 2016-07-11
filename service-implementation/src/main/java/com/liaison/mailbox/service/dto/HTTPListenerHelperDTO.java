/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto;

import java.util.Set;

import com.liaison.mailbox.dtdm.model.ProcessorProperty;

/**
 * Helper DTO to hold http listener details
 * 
 * @author OFS
 *
 */
public class HTTPListenerHelperDTO {
	
	private String processorId;
	private String procsrProtocol;
	private String procsrType;
	private String procsrPropertyJson;
	private String serviceInstanceId;
	private String mbxId;
	private String mbxName;
	private String tenancyKey;
	private String ttlValue;
	private String ttlUnit;
	private Set<ProcessorProperty> dynamicProperties;
	
	public HTTPListenerHelperDTO(String processorId, String procsrProtocol,
			String procsrType, String procsrPropertyJson,
			String serviceInstanceId, String mbxId, String mbxName,
			String tenancyKey, String ttlValue, String ttlUnit,
			Set<ProcessorProperty> dynamicProperties) {

		this.processorId = processorId;
		this.procsrProtocol = procsrProtocol;
		this.procsrType = procsrType;
		this.procsrPropertyJson = procsrPropertyJson;
		this.serviceInstanceId = serviceInstanceId;
		this.mbxId = mbxId;
		this.mbxName = mbxName;
		this.tenancyKey = tenancyKey;
		this.ttlValue = ttlValue;
		this.ttlUnit = ttlUnit;
		this.dynamicProperties = dynamicProperties;
	}
	public String getProcessorId() {
		return processorId;
	}
	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}
	public String getProcsrProtocol() {
		return procsrProtocol;
	}
	public void setProcsrProtocol(String procsrProtocol) {
		this.procsrProtocol = procsrProtocol;
	}
	public String getProcsrType() {
		return procsrType;
	}
	public void setProcsrType(String procsrType) {
		this.procsrType = procsrType;
	}
	public String getProcsrPropertyJson() {
		return procsrPropertyJson;
	}
	public void setProcsrPropertyJson(String procsrPropertyJson) {
		this.procsrPropertyJson = procsrPropertyJson;
	}
	public String getServiceInstanceId() {
		return serviceInstanceId;
	}
	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}
	public String getMbxId() {
		return mbxId;
	}
	public void setMbxId(String mbxId) {
		this.mbxId = mbxId;
	}
	public String getMbxName() {
		return mbxName;
	}
	public void setMbxName(String mbxName) {
		this.mbxName = mbxName;
	}
	public String getTenancyKey() {
		return tenancyKey;
	}
	public void setTenancyKey(String tenancyKey) {
		this.tenancyKey = tenancyKey;
	}
	public String getTtlValue() {
		return ttlValue;
	}
	public void setTtlValue(String ttlValue) {
		this.ttlValue = ttlValue;
	}
	public String getTtlUnit() {
		return ttlUnit;
	}
	public void setTtlUnit(String ttlUnit) {
		this.ttlUnit = ttlUnit;
	}
	public Set<ProcessorProperty> getDynamicProperties() {
		return dynamicProperties;
	}
	public void setDynamicProperties(Set<ProcessorProperty> dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}
}
