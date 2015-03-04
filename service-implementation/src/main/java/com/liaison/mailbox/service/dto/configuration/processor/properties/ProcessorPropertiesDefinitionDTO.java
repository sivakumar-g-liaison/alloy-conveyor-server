package com.liaison.mailbox.service.dto.configuration.processor.properties;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("processorDefinition")
public class ProcessorPropertiesDefinitionDTO {
	
	private String type;
	private String displayName;
	private String protocol;
	private boolean handOverExecutionToJavaScript;
	private List<ProcessorPropertyDTO> staticProperties;
	
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
