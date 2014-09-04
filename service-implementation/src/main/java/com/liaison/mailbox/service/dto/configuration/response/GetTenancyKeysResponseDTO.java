/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.ResponseDTO;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;

/**
 * 
 * @author OFS
 *
 */
@JsonRootName("getTenancyKeysResponse")
public class GetTenancyKeysResponseDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ResponseDTO response;
	private List <TenancyKeyDTO> tenancyKeys;
	
	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

	public List<TenancyKeyDTO> getTenancyKeys() {
		return tenancyKeys;
	}

	public void setTenancyKeys(List<TenancyKeyDTO> tenancyKeys) {
		this.tenancyKeys = tenancyKeys;
	}

}
