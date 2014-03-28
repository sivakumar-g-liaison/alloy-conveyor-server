/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.fsm;

import java.util.ArrayList;
import java.util.List;

import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.jpa.model.FSMState;
import com.liaison.mailbox.jpa.model.FSMStateValue;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.service.util.MailBoxUtility;

/**
 * @author
 * 
 */
public class ProcessorStateDTO {

	private String processorId;
	private String processorName;
	private ProcessorType processorType;
	private String mailboxId;
	private String profileName;
	private ExecutionState executionState;
	private String stateNotes;
	private String executionId;

	public ProcessorStateDTO(String executionId, String processorId, ExecutionState executionState, String processorName,
			ProcessorType processorType, String mailboxId, String profileName, String stateNotes) {

		this.setExecutionId(executionId);
		this.setExecutionState(executionState);
		this.setProcessorId(processorId);
		this.setProcessorName(processorName);
		this.setProcessorType(processorType);
		this.setMailboxId(mailboxId);
		this.setProfileName(profileName);
		this.setStateNotes(stateNotes);
	}
	
	public ProcessorStateDTO(String executionId,String processorId,ExecutionState executionState){
		this.setExecutionId(executionId);
		this.setExecutionState(executionState);
		this.setProcessorId(processorId);
	}

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

	public ProcessorStateDTO createACopyWithNewState(ExecutionState execState) {
		ProcessorStateDTO newCopy = new ProcessorStateDTO(this.getExecutionId(), this.getProcessorId(), execState,
				this.getProcessorName(), this.getProcessorType(), this.getMailboxId(), this.getProfileName(), this.getStateNotes());
		return newCopy;
	}

	public void copyToEntity(FSMState entity) {
		
		//Constructing the FSMStae
		entity.setPguid(MailBoxUtility.getGUID());
		entity.setExecutionId(this.getExecutionId());
		entity.setProcessorId(this.getProcessorId());
		entity.setProcessorName(this.getProcessorName());
		entity.setProcessorType(this.getProcessorType().getCode());
		entity.setMailboxId(this.getMailboxId());
		entity.setProfileName(this.getProfileName());
		entity.setStateNotes(this.getStateNotes());
		
		//Constructing FSMStateValue
		FSMStateValue value = new FSMStateValue();
		value.setPguid(MailBoxUtility.getGUID());
		value.setValue(this.getExecutionState().value());
		value.setCreatedDate(MailBoxUtility.getTimestamp());
		List<FSMStateValue> values = new ArrayList<FSMStateValue>();
		values.add(value);
		entity.setExecutionState(values);
	}
	
	/**
	 * Construct processor state dto from the processor.
	 * 
	 * @param executionId
	 * @param processor
	 * @param profileName
	 * @param state
	 * @param stateNotes
	 * @return
	 */
	public static ProcessorStateDTO getProcessorStateInstance(String executionId, Processor processor, String profileName,
			ExecutionState state, String stateNotes) {

		return new ProcessorStateDTO(executionId, processor.getPguid(), state, processor.getProcsrName(),
				processor.getProcessorType(), processor.getMailbox().getPguid(), profileName, stateNotes);
	}
	
	public boolean equals(ProcessorStateDTO incoming) {
		return (this.executionId.equals(incoming.executionId)
				&& this.executionState.value().equals(incoming.getExecutionState().value()));
	}

}
