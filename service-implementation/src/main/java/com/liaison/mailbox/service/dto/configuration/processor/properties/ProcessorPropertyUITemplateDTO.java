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

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * @author OFS
 *
 */
@JsonRootName("processorDefinition")
public class ProcessorPropertyUITemplateDTO {

	private String type;
	private String displayName;
	private String protocol;
	private boolean handOverExecutionToJavaScript;
	private List<ProcessorPropertyDTO> staticProperties;
	private List<ProcessorFolderPropertyDTO> folderProperties;


	public List<ProcessorFolderPropertyDTO> getFolderProperties() {
		return folderProperties;
	}
	public void setFolderProperties(
			List<ProcessorFolderPropertyDTO> folderProperties) {
		this.folderProperties = folderProperties;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public boolean isHandOverExecutionToJavaScript() {
		return handOverExecutionToJavaScript;
	}
	public void setHandOverExecutionToJavaScript(boolean handOverExecutionToJavaScript) {
		this.handOverExecutionToJavaScript = handOverExecutionToJavaScript;
	}
	public List<ProcessorPropertyDTO> getStaticProperties() {
		return staticProperties;
	}
	public void setStaticProperties(List<ProcessorPropertyDTO> staticProperties) {
		this.staticProperties = staticProperties;
	}

}
