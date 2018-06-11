/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto;

import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.glass.util.ExecutionTimestamp;

import java.util.Date;

public class GlassMessageDTO {
    
    private String globalProcessId;
    private ProcessorType processorType;
    private String processProtocol;
    private String fileName;
    private String filePath;
    private long fileLength;
    private ExecutionState status;
    private String message;
    private String pipelineId;
    private ExecutionTimestamp firstCornerTimeStamp;
    private String senderIp;
    private String receiverIp;
    private String relatedTransactionId;
    private Date statusDate;
    
    public String getRelatedTransactionId() {
        return relatedTransactionId;
    }
    public void setRelatedTransactionId(String relatedTransactionId) {
        this.relatedTransactionId = relatedTransactionId;
    }
	public String getGlobalProcessId() {
        return globalProcessId;
    }
    public void setGlobalProcessId(String globalProcessId) {
        this.globalProcessId = globalProcessId;
    }
    public ProcessorType getProcessorType() {
        return processorType;
    }
    public void setProcessorType(ProcessorType processorType) {
        this.processorType = processorType;
    }
    public String getProcessProtocol() {
        return processProtocol;
    }
    public void setProcessProtocol(String processProtocol) {
        this.processProtocol = processProtocol;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public long getFileLength() {
        return fileLength;
    }
    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
    public ExecutionState getStatus() {
        return status;
    }
    public void setStatus(ExecutionState status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getPipelineId() {
        return pipelineId;
    }
    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }
    public ExecutionTimestamp getFirstCornerTimeStamp() {
        return firstCornerTimeStamp;
    }
    public void setFirstCornerTimeStamp(ExecutionTimestamp firstCornerTimeStamp) {
        this.firstCornerTimeStamp = firstCornerTimeStamp;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public String getReceiverIp() {
        return receiverIp;
    }

    public void setReceiverIp(String receiverIp) {
        this.receiverIp = receiverIp;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }
}
