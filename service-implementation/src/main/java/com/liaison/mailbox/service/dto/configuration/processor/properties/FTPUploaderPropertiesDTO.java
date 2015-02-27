package com.liaison.mailbox.service.dto.configuration.processor.properties;

public class FTPUploaderPropertiesDTO extends StaticProcessorPropertiesDTO {
	
	private String url;
	private int port;
	private int retryAttempts;
	private int socketTimeout;
	private int connectionTimeout;
	private boolean chunkedEncoding;
	private String contentType;
	private boolean passive;
	private boolean binary;
	private int retryInterval;
	private String errorFileLocation;
	private String processedFileLocation;
	
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
	public boolean isPassive() {
		return passive;
	}
	public void setPassive(boolean passive) {
		this.passive = passive;
	}
	public boolean isBinary() {
		return binary;
	}
	public void setBinary(boolean binary) {
		this.binary = binary;
	}
	public int getRetryInterval() {
		return retryInterval;
	}
	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}
	public String getErrorFileLocation() {
		return errorFileLocation;
	}
	public void setErrorFileLocation(String errorFileLocation) {
		this.errorFileLocation = errorFileLocation;
	}
	public String getProcessedFileLocation() {
		return processedFileLocation;
	}
	public void setProcessedFileLocation(String processedFileLocation) {
		this.processedFileLocation = processedFileLocation;
	}		

}
