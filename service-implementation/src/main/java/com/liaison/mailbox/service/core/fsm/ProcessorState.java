package com.liaison.mailbox.service.core.fsm;

import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;

public class ProcessorState {
	
	private String processorId;
	private String processorName;
	private ProcessorType processorType;
	private String mailboxId;
	private String profileName;
	private ExecutionState executionState;
	private String stateNotes;
	
	private String executionId;
	public String getExecutionId() {
		return executionId;
	}
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}
	public String getProcessorId() {
		return processorId;
	}
	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}
	public String getProcessorName() {
		return processorName;
	}
	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}
	public ProcessorType getProcessorType() {
		return processorType;
	}
	public void setProcessorType(ProcessorType processorType) {
		this.processorType = processorType;
	}
	public String getMailboxId() {
		return mailboxId;
	}
	public void setMailboxId(String mailboxId) {
		this.mailboxId = mailboxId;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	public String getStateNotes() {
		return stateNotes;
	}
	public void setStateNotes(String stateNotes) {
		this.stateNotes = stateNotes;
	}
	
	public ExecutionState getExecutionState() {
		return executionState;
	}
	public void setExecutionState(ExecutionState executionState) {
		this.executionState = executionState;
	}
	
	//TODO make a deep copy of everything.
	public ProcessorState copyFrom(ProcessorState state,ExecutionState execState){
		 this.setExecutionId(state.getExecutionId());
		 this.setExecutionState(execState);
		 return this;
	}
	
	public boolean equals(ProcessorState incoming){
		System.out.println("Trying to compare if the state object has ID "+incoming.executionId);
		return (this.executionId == incoming.executionId && this.executionState.name() == incoming.getExecutionState().name());
		
		
	}

}
