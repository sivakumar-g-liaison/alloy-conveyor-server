/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.processor.properties;

import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * Data Transfer Object for the properties of Lite HTTP processor.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "staticProperties")
public class LiteHTTPPropertiesDTO extends StaticProcessorPropertiesDTO {

    private String httpListenerPipeLineId;
    private boolean httpListenerAuthCheckRequired;

    public String getHttpListenerPipeLineId() {
        return httpListenerPipeLineId;
    }

    public void setHttpListenerPipeLineId(String httpListenerPipeLineId) {
        this.httpListenerPipeLineId = httpListenerPipeLineId;
    }

    public boolean isHttpListenerAuthCheckRequired() {
        return httpListenerAuthCheckRequired;
    }

    public void setHttpListenerAuthCheckRequired(
            boolean httpListenerAuthCheckRequired) {
        this.httpListenerAuthCheckRequired = httpListenerAuthCheckRequired;
    }
}
