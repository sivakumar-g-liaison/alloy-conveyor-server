package com.liaison.mailbox.jpa.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.fsm.ActiveEvent;
import com.liaison.fsm.Event;
import com.liaison.fsm.FSMDao;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.FSMModel;
import com.liaison.mailbox.service.core.fsm.ProcessorState;

public class MailBoxFSMDAOBase extends GenericDAOBase<FSMModel> implements FSMDao<ProcessorState, ExecutionEvents> {
	
	
	private static Map<String,ProcessorState> dummyProcessorStateMap = new HashMap<>(); // TODO REMOVE THIS LATER THIS IS JUST TO MOCK THE DB
	
	@Override
	public void addState(String executionId, ProcessorState state) {
		
		//TODO Fill model FSMMOdel and persist in DB. ALWAYS INSERT NEW STATE DO NOT UPDATE PREVIOUS ONE.
		dummyProcessorStateMap.put(executionId, state);
		System.out.println("The STATE of "+executionId+" is "+state.getExecutionState());
	}



	@Override
	public ProcessorState getState(String executionId) {
		
		//TODO Fetch  the state from DB for the incoming execution id, 
		//since we always add states we will have more than one state entry for the given execution ID so always get the latest one.	
		return dummyProcessorStateMap.get(executionId);
	}
	
	
	@Override
	public Event<ExecutionEvents> createEvent(ExecutionEvents executionEvent) {
		Event<ExecutionEvents> event = new ActiveEvent<ExecutionEvents>(executionEvent);
		//events.add(event);
		return event;
	}



	@Override
	public void deleteStates(List<String> arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public List<ExecutionEvents> getEvents() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Map<String, ProcessorState> getStates() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setState(String arg0, ProcessorState arg1) {
		// TODO Auto-generated method stub
		
	}

	
	

}
