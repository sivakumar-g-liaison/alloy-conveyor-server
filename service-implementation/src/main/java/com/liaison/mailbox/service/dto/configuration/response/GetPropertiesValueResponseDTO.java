/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

/**
 *
 *
 * @author OFS
 */

package com.liaison.mailbox.service.dto.configuration.response;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;
import com.liaison.mailbox.service.dto.configuration.PropertiesFileDTO;

/**
 * Data Transfer Object used for retrieving the mailbox properties.
 *
 * @author OFS
 */
@JsonRootName("getPropertiesValueResponseDTO")
public class GetPropertiesValueResponseDTO extends CommonResponseDTO {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private PropertiesFileDTO properties;

	public PropertiesFileDTO getProperties() {
		return properties;
	}

	public void setProperties(PropertiesFileDTO properties) {
		this.properties = properties;
	}
}
