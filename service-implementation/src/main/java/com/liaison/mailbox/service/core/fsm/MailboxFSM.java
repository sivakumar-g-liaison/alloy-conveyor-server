package com.liaison.mailbox.service.core.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.fsm.ActiveEvent;
import com.liaison.fsm.ActiveTransition;
import com.liaison.fsm.Delegate;
import com.liaison.fsm.Event;
import com.liaison.fsm.FSM;
import com.liaison.fsm.FSMDao;
import com.liaison.fsm.Transition;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.jpa.dao.MailBoxFSMDAOBase;
import com.liaison.mailbox.service.exception.MailBoxFSMSetupException;

public class MailboxFSM implements FSM<ProcessorState,ExecutionEvents>{
	
	
	
	public MailboxFSM(){
		this.setFSMDao(new MailBoxFSMDAOBase());
	}
	
	Delegate<ProcessorState,ExecutionEvents> delegateMiss = null;
	List<Transition<ProcessorState,ExecutionEvents>> transitions = new ArrayList<>();
	FSMDao<ProcessorState,ExecutionEvents> fsmDao = null;

	@Override
	public void addState(String strName, ProcessorState state) {
		fsmDao.addState(strName, state);
		
	}
	
	public void addState(ProcessorState state) {
		this.addState(state.getExecutionId(), state);
		
	}

	@Override
	public void addTransition(Transition<ProcessorState, ExecutionEvents> transition) {
		transitions.add(transition);
		
	}

	@Override
	public Event<ExecutionEvents> createEvent(ExecutionEvents eventType) {
		return fsmDao.createEvent(eventType);
	}

	@Override
	public Transition<ProcessorState, ExecutionEvents> createTransition() {
		return new ActiveTransition<ProcessorState, ExecutionEvents>();
	}

	@Override
	public Delegate<ProcessorState, ExecutionEvents> getDelegateMiss() {
		return delegateMiss;
	}

	@Override
	public ProcessorState getState(String stateId) {
		return fsmDao.getState(stateId);
	}

	@Override
	public void handleEvent(Event<ExecutionEvents> event) {
		Transition<ProcessorState, ExecutionEvents> transition = null;
		for (Transition<ProcessorState, ExecutionEvents> trans : transitions) {
			if (trans.getEvent().getEventType().equals(event.getEventType())) {
				boolean bMatch = true;				
				for (Entry<String, ProcessorState> entry : trans.getCriteria().entrySet()) {
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
			for (Entry<String, ProcessorState> entry : transition.getUpdate().entrySet()) {
				/** TODO (RKOH): separate function for setState? */
				this.fsmDao.addState(entry.getKey(), entry.getValue());
			}

			Delegate<ProcessorState, ExecutionEvents> delegate = transition.getDelegateMatch();
			if (delegate != null) {
				Event<ExecutionEvents> e = delegate.perform(this, event);
				if (e != null) {
					handleEvent(e);
				}
			}
		}
		else {
			Delegate<ProcessorState, ExecutionEvents> delegate = getDelegateMiss();
			if (delegate != null) {
				delegate.perform(this, event);
			}
		}
		
	}

	@Override
	public void printStates() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDelegateMiss(Delegate<ProcessorState, ExecutionEvents> delegate) {
		this.delegateMiss = delegate;
		
	}

	@Override
	public void setFSMDao(FSMDao<ProcessorState, ExecutionEvents> fsmDao) {
		this.fsmDao = fsmDao;
		
	}
	
	public boolean addDefaultStateTransitionRules(ProcessorState processorQueued) throws MailBoxFSMSetupException{
		if(processorQueued.getExecutionState().value() != ExecutionState.QUEUED.value()){
			
			throw new MailBoxFSMSetupException("The Processor should be in the QUEUED status to use the default rules");
		}
		
		//Transition Rules - QUEUED TO PROCESSING WHEN ExecutionEvents.PROCESSOR_EXECUTION_STARTED is passed on
		Transition<ProcessorState,ExecutionEvents> transition = this.createTransition();        
		transition.addCriteria(processorQueued.getExecutionId(), processorQueued);
		transition.setEvent( new ActiveEvent<ExecutionEvents>(ExecutionEvents.PROCESSOR_EXECUTION_STARTED) );	
		ProcessorState processorProcessing = processorQueued.createACopyWithNewState(ExecutionState.PROCESSING);
		transition.addUpdate( processorProcessing.getExecutionId(), processorProcessing);        
		this.addTransition( transition );
			    
		//Transition Rules - PROCESSING TO COMPLTED WHEN ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED is passed on
		transition = this.createTransition();        
		transition.addCriteria(processorProcessing.getExecutionId(), processorProcessing);
		transition.setEvent(new ActiveEvent<ExecutionEvents>(ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED) );	   
		ProcessorState processorCompleted = processorProcessing.createACopyWithNewState(ExecutionState.COMPLETED);
		transition.addUpdate( processorCompleted.getExecutionId(), processorCompleted);        
		this.addTransition( transition );
		
		//Transition Rules - PROCESSING TO FAILED WHEN ExecutionEvents.PROCESSOR_EXECUTION_FAILED is passed on
		transition = this.createTransition();        
		transition.addCriteria(processorProcessing.getExecutionId(), processorProcessing);
		transition.setEvent(new ActiveEvent<ExecutionEvents>(ExecutionEvents.PROCESSOR_EXECUTION_FAILED) );	   
		ProcessorState processorFailed = processorProcessing.createACopyWithNewState(ExecutionState.FAILED);
		transition.addUpdate( processorFailed.getExecutionId(), processorFailed);        
		this.addTransition( transition );
		return true;
	}

	
}

