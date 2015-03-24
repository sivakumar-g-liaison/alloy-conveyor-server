package com.liaison.mailbox.service.util;

import com.liaison.commons.message.glass.dom.GatewayType;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;

public class GlassMessage {

	private ProcessorType category;
	private ExecutionState status;
	private String globalPId;
	private String mailboxId;
	private String processorId;
	private String executionId;
	private String tenancyKey;
	private String serviceInstandId;
    private String protocol;
    private String pipelineId;
    private GatewayType inAgent;
    private GatewayType outAgent;
    

	public GatewayType getOutAgent() {
		return outAgent;
	}

	public void setOutAgent(GatewayType outAgent) {
		this.outAgent = outAgent;
	}

	public GatewayType getInAgent() {
		return inAgent;
	}

	public void setInAgent(GatewayType inAgent) {
		this.inAgent = inAgent;
	}

	public String getPipelineId() {
		return pipelineId;
	}

	public void setPipelineId(String pipelineId) {
		this.pipelineId = pipelineId;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public ProcessorType getCategory() {
		return category;
	}

	public void setCategory(ProcessorType category) {
		this.category = category;
	}

	public ExecutionState getStatus() {
		return status;
	}

	public void setStatus(ExecutionState status) {
		this.status = status;
	}

	public String getGlobalPId() {
		return globalPId;
	}

	public void setGlobalPId(String globalPId) {
		this.globalPId = globalPId;
	}

	public String getMailboxId() {
		return mailboxId;
	}

	public void setMailboxId(String mailboxId) {
		this.mailboxId = mailboxId;
	}

	public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getTenancyKey() {
		return tenancyKey;
	}

	public void setTenancyKey(String tenancyKey) {
		this.tenancyKey = tenancyKey;
	}

	public String getServiceInstandId() {
		return serviceInstandId;
	}

	public void setServiceInstandId(String serviceInstandId) {
		this.serviceInstandId = serviceInstandId;
	}

}
