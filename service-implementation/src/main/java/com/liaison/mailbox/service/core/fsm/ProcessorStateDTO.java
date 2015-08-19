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

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.model.FSMState;
import com.liaison.mailbox.rtdm.model.FSMStateValue;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * @author OFS
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
	private String slaVerficationStatus;


	public ProcessorStateDTO(String executionId,String processorId,ExecutionState executionState){
		this.setExecutionId(executionId);
		this.setExecutionState(executionState);
		this.setProcessorId(processorId);
	}

	public ProcessorStateDTO() {

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

	public String getSlaVerficationStatus() {
		return slaVerficationStatus;
	}

	public void setSlaVerficationStatus(String slaVerficationStatus) {
		this.slaVerficationStatus = slaVerficationStatus;
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

	public ProcessorStateDTO cloneWithNewState(ExecutionState execState) {
		ProcessorStateDTO newCopy = new ProcessorStateDTO();
		newCopy.setExecutionId(this.getExecutionId());
		newCopy.setProcessorId(this.getProcessorId());
		newCopy.setExecutionState(execState);
		newCopy.setProcessorName(this.getProcessorName());
		newCopy.setProcessorType(this.getProcessorType());
		newCopy.setMailboxId(this.getMailboxId());
		newCopy.setProfileName(this.getProfileName());
		newCopy.setStateNotes(this.getStateNotes());
		newCopy.setSlaVerficationStatus(this.getSlaVerficationStatus());
		return newCopy;
	}

	/**
	 * Copies all the data from dto to entity.
	 *
	 * @param entity
	 *         the FSMState
	 */
	public void copyToEntity(FSMState entity) {

		//Constructing the FSMStae
		entity.setPguid(MailBoxUtil.getGUID());
		entity.setExecutionId(this.getExecutionId());
		entity.setProcessorId(this.getProcessorId());
		entity.setProcessorName(this.getProcessorName());
		entity.setProcessorType(this.getProcessorType().getCode());
		entity.setMailboxId(this.getMailboxId());
		entity.setProfileName(this.getProfileName());
		entity.setStateNotes(this.getExecutionState().notes());
		entity.setSlaVerificationStatus(this.getSlaVerficationStatus());

		//Constructing FSMStateValue
		FSMStateValue value = new FSMStateValue();
		value.setPguid(MailBoxUtil.getGUID());
		value.setValue(this.getExecutionState().value());
		value.setFsmState(entity);
		value.setCreatedDate(MailBoxUtil.getTimestamp());
		List<FSMStateValue> values = new ArrayList<FSMStateValue>();
		values.add(value);
		entity.setExecutionState(values);
	}

	/**
	 * Copies all the data from processor to dto.
	 *
	 * @param executionId
	 * @param processor
	 * @param profileName
	 * @param state
	 * @param stateNotes
	 * @return ProcessorStateDTO
	 */

	public void setValues(String executionId, Processor processor, String profileName,ExecutionState state, String slaVerificationStatus) {

		this.setExecutionId(executionId);
		this.setProcessorId(processor.getPguid());
		this.setExecutionState(state);
		this.setProcessorName(processor.getProcsrName());
		this.setProcessorType(processor.getProcessorType());
		this.setMailboxId(processor.getMailbox().getPguid());
		this.setProfileName(profileName);
		this.setSlaVerficationStatus(slaVerificationStatus);

	}

	/**
	 * Validation for  executionId.
	 * @param incoming
	 *           executionId from ProcessorStateDTO
	 * @return true validation passed
	 */
	public boolean equals(ProcessorStateDTO incoming) {
		return (this.executionId.equals(incoming.executionId)
				&& this.executionState.value().equals(incoming.getExecutionState().value()));
	}

}
