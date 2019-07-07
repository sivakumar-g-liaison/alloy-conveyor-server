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

import java.io.Serializable;

/**
* Class that contains the response.
*
* @author OFS
*/
public abstract class CommonResponseDTO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ResponseDTO response;
	
	public ResponseDTO getResponse() {
		return response;
	}

	public void setResponse(ResponseDTO response) {
		this.response = response;
	}

}
