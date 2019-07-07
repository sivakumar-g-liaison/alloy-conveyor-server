/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.request;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * Data Transfer Object that contains the fields required for script creation or revision in gitlab.
 * 
 * @author OFS
 */
@JsonRootName("scriptserviceRequest")
public class ScriptServiceRequestDTO {
	
	private ScriptServiceDTO script;

	public ScriptServiceDTO getScript() {
		return script;
	}

	public void setScript(ScriptServiceDTO script) {
		this.script = script;
	}
	
}
