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
import java.util.Map.Entry;

import com.liaison.fsm.ActiveEvent;
import com.liaison.fsm.ActiveTransition;
import com.liaison.fsm.Delegate;
import com.liaison.fsm.Event;
import com.liaison.fsm.FSM;
import com.liaison.fsm.FSMDao;
import com.liaison.fsm.Transition;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.jpa.dao.FSMStateDAOBase;
import com.liaison.mailbox.service.exception.MailBoxFSMSetupException;

public class MailboxFSM implements FSM<ProcessorStateDTO, ExecutionEvents> {

	Delegate<ProcessorStateDTO, ExecutionEvents> delegateMiss = null;
	List<Transition<ProcessorStateDTO, ExecutionEvents>> transitions = new ArrayList<>();
	FSMDao<ProcessorStateDTO, ExecutionEvents> fsmDao = null;

	public MailboxFSM() {
		this.setFSMDao(new FSMStateDAOBase());
	}

	@Override
	public void addState(String executionId, ProcessorStateDTO state) {
		fsmDao.addState(executionId, state);

	}

	public void addState(ProcessorStateDTO state) {
		this.addState(state.getExecutionId(), state);

	}

	@Override
	public void addTransition(Transition<ProcessorStateDTO, ExecutionEvents> transition) {
		transitions.add(transition);

	}

	@Override
	public Event<ExecutionEvents> createEvent(ExecutionEvents eventType) {
		return fsmDao.createEvent(eventType);
	}

	@Override
	public Transition<ProcessorStateDTO, ExecutionEvents> createTransition() {
		return new ActiveTransition<ProcessorStateDTO, ExecutionEvents>();
	}

	@Override
	public Delegate<ProcessorStateDTO, ExecutionEvents> getDelegateMiss() {
		return delegateMiss;
	}

	@Override
	public ProcessorStateDTO getState(String stateId) {
		return fsmDao.getState(stateId);
	}

	@Override
	public void handleEvent(Event<ExecutionEvents> event) {

		Transition<ProcessorStateDTO, ExecutionEvents> transition = null;
		for (Transition<ProcessorStateDTO, ExecutionEvents> trans : transitions) {
			
			if (trans.getEvent().getEventType().equals(event.getEventType())) {
				
				boolean bMatch = true;
				for (Entry<String, ProcessorStateDTO> entry : trans.getCriteria().entrySet()) {
					
					if (!entry.getValue().equals(fsmDao.getState(entry.getKey()))) {
						bMatch = false;
						break;
					}
				}

				if (bMatch) {
					transition = trans;
					break;
				}
			}
		}

		if (transition != null) {
			for (Entry<String, ProcessorStateDTO> entry : transition.getUpdate().entrySet()) {
				/** TODO (RKOH): separate function for setState? */
				
				fsmDao.setState(entry.getKey(), entry.getValue());
			}

			Delegate<ProcessorStateDTO, ExecutionEvents> delegate = transition.getDelegateMatch();
			if (delegate != null) {
				Event<ExecutionEvents> e = delegate.perform(this, event);
				if (e != null) {
					handleEvent(e);
				}
			}
		} else {
			Delegate<ProcessorStateDTO, ExecutionEvents> delegate = getDelegateMiss();
			if (delegate != null) {
				delegate.perform(this, event);
			}
		}

	}

	@Override
	public void setDelegateMiss(Delegate<ProcessorStateDTO, ExecutionEvents> delegate) {
		this.delegateMiss = delegate;

	}

	@Override
	public void setFSMDao(FSMDao<ProcessorStateDTO, ExecutionEvents> fsmDao) {
		this.fsmDao = fsmDao;

	}

	public boolean addDefaultStateTransitionRules(ProcessorStateDTO processorQueued) throws MailBoxFSMSetupException {

		if (!processorQueued.getExecutionState().value().equals(ExecutionState.QUEUED.value())) {
			throw new MailBoxFSMSetupException("The Processor should be in the QUEUED status to use the default rules");
		}

		// Transition Rules - QUEUED TO PROCESSING WHEN ExecutionEvents.PROCESSOR_EXECUTION_STARTED
		// is passed on
		Transition<ProcessorStateDTO, ExecutionEvents> transition = this.createTransition();
		transition.addCriteria(processorQueued.getExecutionId(), processorQueued);
		transition.setEvent(new ActiveEvent<ExecutionEvents>(ExecutionEvents.PROCESSOR_EXECUTION_STARTED));
		ProcessorStateDTO processorProcessing = processorQueued.createACopyWithNewState(ExecutionState.PROCESSING);
		transition.addUpdate(processorProcessing.getExecutionId(), processorProcessing);
		this.addTransition(transition);

		// Transition Rules - PROCESSING TO COMPLTED WHEN
		// ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED is passed on
		transition = this.createTransition();
		transition.addCriteria(processorProcessing.getExecutionId(), processorProcessing);
		transition.setEvent(new ActiveEvent<ExecutionEvents>(ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED));
		ProcessorStateDTO processorCompleted = processorProcessing.createACopyWithNewState(ExecutionState.COMPLETED);
		transition.addUpdate(processorCompleted.getExecutionId(), processorCompleted);
		this.addTransition(transition);

		// Transition Rules - PROCESSING TO FAILED WHEN ExecutionEvents.PROCESSOR_EXECUTION_FAILED
		// is passed on
		transition = this.createTransition();
		transition.addCriteria(processorProcessing.getExecutionId(), processorProcessing);
		transition.setEvent(new ActiveEvent<ExecutionEvents>(ExecutionEvents.PROCESSOR_EXECUTION_FAILED));
		ProcessorStateDTO processorFailed = processorProcessing.createACopyWithNewState(ExecutionState.FAILED);
		transition.addUpdate(processorFailed.getExecutionId(), processorFailed);
		this.addTransition(transition);
		return true;
	}

	@Override
	public void setState(String arg0, ProcessorStateDTO arg1) {
		// TODO Auto-generated method stub

	}

}
