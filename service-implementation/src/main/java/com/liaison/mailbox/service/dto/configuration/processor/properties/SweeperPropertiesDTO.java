package com.liaison.mailbox.service.dto.configuration.processor.properties;

public class SweeperPropertiesDTO extends StaticProcessorPropertiesDTO {

	private String pipeLineID;
	private boolean securedPayload;
	private boolean deleteFileAfterSweep;
	private String fileRenameFormat;
	private String numOfFilesThreshold;
	private String payloadSizeThreshold;
	private String sweepedFileLocation;
	
	public String getPipeLineID() {
		return pipeLineID;
	}
	public void setPipeLineID(String pipeLineID) {
		this.pipeLineID = pipeLineID;
	}
	public boolean isSecuredPayload() {
		return securedPayload;
	}
	public void setSecuredPayload(boolean securedPayload) {
		this.securedPayload = securedPayload;
	}
	public boolean isDeleteFileAfterSweep() {
		return deleteFileAfterSweep;
	}
	public void setDeleteFileAfterSweep(boolean deleteFileAfterSweep) {
		this.deleteFileAfterSweep = deleteFileAfterSweep;
	}
	public String getFileRenameFormat() {
		return fileRenameFormat;
	}
	public void setFileRenameFormat(String fileRenameFormat) {
		this.fileRenameFormat = fileRenameFormat;
	}
	public String getNumOfFilesThreshold() {
		return numOfFilesThreshold;
	}
	public void setNumOfFilesThreshold(String numOfFilesThreshold) {
		this.numOfFilesThreshold = numOfFilesThreshold;
	}
	public String getPayloadSizeThreshold() {
		return payloadSizeThreshold;
	}
	public void setPayloadSizeThreshold(String payloadSizeThreshold) {
		this.payloadSizeThreshold = payloadSizeThreshold;
	}
	public String getSweepedFileLocation() {
		return sweepedFileLocation;
	}
	public void setSweepedFileLocation(String sweepedFileLocation) {
		this.sweepedFileLocation = sweepedFileLocation;
	}
}
