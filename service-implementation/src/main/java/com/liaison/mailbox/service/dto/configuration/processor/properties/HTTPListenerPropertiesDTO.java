package com.liaison.mailbox.service.dto.configuration.processor.properties;

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
