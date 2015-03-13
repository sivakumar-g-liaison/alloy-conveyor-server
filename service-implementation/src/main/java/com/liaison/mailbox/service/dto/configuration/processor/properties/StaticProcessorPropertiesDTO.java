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

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author OFS
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
@JsonSubTypes({
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.FTPUploaderPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.FTPDownloaderPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPUploaderPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPDownloaderPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPUploaderPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.SFTPDownloaderPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.SweeperPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.FileWriterPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.DropboxProcessorPropertiesDTO.class),
        @JsonSubTypes.Type(value=com.liaison.mailbox.service.dto.configuration.processor.properties.HTTPListenerPropertiesDTO.class)
})
public class StaticProcessorPropertiesDTO {

	private boolean handOverExecutionToJavaScript;

	public boolean isHandOverExecutionToJavaScript() {
		return handOverExecutionToJavaScript;
	}
	public void setHandOverExecutionToJavaScript(boolean handOverExecutionToJavaScript) {
		this.handOverExecutionToJavaScript = handOverExecutionToJavaScript;
	}




}
