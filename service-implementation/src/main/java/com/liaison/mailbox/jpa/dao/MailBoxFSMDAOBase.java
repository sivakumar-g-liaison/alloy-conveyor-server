package com.liaison.mailbox.jpa.dao;

import java.util.LinkedList;
import java.util.List;

import org.apache.xbean.finder.AnnotationFinder.SubArchive.E;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.fsm.ActiveEvent;
import com.liaison.fsm.Event;
import com.liaison.fsm.FSMDao;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.jpa.model.FSMModel;
import com.liaison.mailbox.service.core.fsm.ProcessorState;

public class MailBoxFSMDAOBase extends GenericDAOBase<FSMModel> implements FSMDao<ProcessorState, ExecutionEvents> {
	
	private List<Event<ExecutionEvents>> events = new LinkedList<Event<ExecutionEvents>>();
	private ProcessorState state = null;
	@Override
	public void addState(String arg0, ProcessorState arg1) {
		System.out.println("The State of "+arg1.getExecutionId() + " now is "+arg1.getExecutionState());
		//Fill model FSMMOdel and persist in DB
		this.state= arg1;
	}



	@Override
	public ProcessorState getState(String arg0) {
		System.out.println("Get state called with "+arg0);
				
		return state;
	}
	
	
	@Override
	public Event<ExecutionEvents> createEvent(ExecutionEvents arg0) {
		Event<ExecutionEvents> event = new ActiveEvent<ExecutionEvents>(arg0);
		events.add(event);
		return event;
	}

	
	

}
