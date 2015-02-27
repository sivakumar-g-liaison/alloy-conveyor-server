package com.liaison.mailbox.service.dto.configuration.processor.properties;

public class DropboxProcessorPropertiesDTO extends StaticProcessorPropertiesDTO {
	
	private String httpListenerPipeLineId;
	private boolean securedPayload;
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

}
