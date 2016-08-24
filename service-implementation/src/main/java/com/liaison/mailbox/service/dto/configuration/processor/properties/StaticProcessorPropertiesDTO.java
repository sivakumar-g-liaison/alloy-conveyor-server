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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.validation.PatternValidation;

/**
 * Data Transfer Object for the static properties of processor.
 * 
 * @author OFS
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "staticProperties")
@JsonSubTypes({ @JsonSubTypes.Type(value = FTPUploaderPropertiesDTO.class),
		@JsonSubTypes.Type(value = FTPDownloaderPropertiesDTO.class),
		@JsonSubTypes.Type(value = HTTPUploaderPropertiesDTO.class),
		@JsonSubTypes.Type(value = HTTPDownloaderPropertiesDTO.class),
		@JsonSubTypes.Type(value = SFTPUploaderPropertiesDTO.class),
		@JsonSubTypes.Type(value = SFTPDownloaderPropertiesDTO.class),
		@JsonSubTypes.Type(value = SweeperPropertiesDTO.class),
		@JsonSubTypes.Type(value = FileWriterPropertiesDTO.class),
		@JsonSubTypes.Type(value = DropboxProcessorPropertiesDTO.class),
		@JsonSubTypes.Type(value = HTTPListenerPropertiesDTO.class) })
public class StaticProcessorPropertiesDTO {

	private boolean handOverExecutionToJavaScript;

	public boolean isHandOverExecutionToJavaScript() {
		return handOverExecutionToJavaScript;
	}
	public void setHandOverExecutionToJavaScript(boolean handOverExecutionToJavaScript) {
		this.handOverExecutionToJavaScript = handOverExecutionToJavaScript;
	}

}
