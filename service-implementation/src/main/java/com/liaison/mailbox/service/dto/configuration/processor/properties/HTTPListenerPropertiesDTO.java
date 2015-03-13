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

import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author OFS
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
public class HTTPListenerPropertiesDTO extends StaticProcessorPropertiesDTO {

	private String httpListenerPipeLineId;
	private boolean securedPayload;
	private boolean httpListenerAuthCheckRequired;

	public String getHttpListenerPipeLineId() {
		return httpListenerPipeLineId;
	}
	public void setHttpListenerPipeLineId(String httpListenerPipeLineId) {
		this.httpListenerPipeLineId = httpListenerPipeLineId;
	}
	public boolean isSecuredPayload() {
		return securedPayload;
	}
	public void setSecuredPayload(boolean securedPayload) {
		this.securedPayload = securedPayload;
	}
	public boolean isHttpListenerAuthCheckRequired() {
		return httpListenerAuthCheckRequired;
	}
	public void setHttpListenerAuthCheckRequired(
			boolean httpListenerAuthCheckRequired) {
		this.httpListenerAuthCheckRequired = httpListenerAuthCheckRequired;
	}

}
