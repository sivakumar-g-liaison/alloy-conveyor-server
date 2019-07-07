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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * Data Transfer Object for dynamic properties.
 * 
 * @author OFS
 */
@JsonRootName("dynamicProperties")
public class DynamicPropertiesDTO {

	private List<PropertyDTO> dynamicProperties;

	public List<PropertyDTO> getDynamicProperties() {

		if (dynamicProperties == null) {
			dynamicProperties = new ArrayList<PropertyDTO>();
		}
		return dynamicProperties;
	}

	public void setDynamicProperties(List<PropertyDTO> dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}
}
