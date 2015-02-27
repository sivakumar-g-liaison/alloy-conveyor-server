package com.liaison.mailbox.service.dto.configuration.processor.properties;


public class HTTPDownloaderPropertiesDTO extends StaticProcessorPropertiesDTO {
	
	private String httpVersion;
	private String httpVerb;
	private int retryAttempts;
	private int socketTimeout;
	private int connectionTimeout;
	private String url;
	private int port;
	private boolean chunkedEncoding;
	private String contentType;
	private String otherRequestHeader;
	public String getHttpVersion() {
		return httpVersion;
	}
	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}
	public String getHttpVerb() {
		return httpVerb;
	}
	public void setHttpVerb(String httpVerb) {
		this.httpVerb = httpVerb;
	}
	public int getRetryAttempts() {
		return retryAttempts;
	}
	public void setRetryAttempts(int retryAttempts) {
		this.retryAttempts = retryAttempts;
	}
	public int getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isChunkedEncoding() {
		return chunkedEncoding;
	}
	public void setChunkedEncoding(boolean chunkedEncoding) {
		this.chunkedEncoding = chunkedEncoding;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getOtherRequestHeader() {
		return otherRequestHeader;
	}
	public void setOtherRequestHeader(String otherRequestHeader) {
		this.otherRequestHeader = otherRequestHeader;
	}

}
