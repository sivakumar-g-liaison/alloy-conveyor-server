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

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.ResponseDTO;
/**
 * 
 * 
 * @author OFS
 */
@JsonRootName("scriptserviceResponse")
public class ScriptServiceResponseDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
    private String script;	
    private ResponseDTO response;
    
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public ResponseDTO getResponse() {
		return response;
	}
	public void setResponse(ResponseDTO response) {
		this.response = response;
	}
    
}
