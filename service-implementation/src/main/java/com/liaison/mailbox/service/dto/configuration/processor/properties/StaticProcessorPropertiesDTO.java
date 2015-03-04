package com.liaison.mailbox.service.dto.configuration.processor.properties;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("staticProperties")
public class StaticProcessorPropertiesDTO {
	
	private boolean handOverExecutionToJavaScript;

	public boolean isHandOverExecutionToJavaScript() {
		return handOverExecutionToJavaScript;
	}

	public void setHandOverExecutionToJavaScript(boolean handOverExecutionToJavaScript) {
		this.handOverExecutionToJavaScript = handOverExecutionToJavaScript;
	} 
	
	
	

}
