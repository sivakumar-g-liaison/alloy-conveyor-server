/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.services.unit.test;

import org.junit.Test;

import com.liaison.fsm.ActiveEvent;
import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorState;

/**
 * 
 * 
 * @author veerasamyn
 */
public class FSMTest extends BaseServiceTest {

	

	@Test
	public void testFSM() throws InterruptedException{
        //STEP 1: BUILD THIS AS SOON AS YOU GET A PROCESSOR FROM DB AFTER TRIGGERING A PROFILE. FILL AS MUCH AS YOU CAN IN stateDefination BUT MUST HAVE EXECUTION ID,PROCESSOR ID and PROFILE NAME 
		ProcessorState stateDefination = new ProcessorState();
		stateDefination.setExecutionId("EXECUTION_ID_1"); //THIS SHOULD BE AN ACTUAL UDID GENERATE ONE THIS WILL BE A NEW ID USED BY FSM.YOU SHOULD ALSO POST THIS ALONG WITH THE PROCESSOR ID TO THE QUEUE.
		stateDefination.setExecutionState(ExecutionState.QUEUED);
		
		//STEP 2: INITAIALZE FSM INSTANCE		
		MailboxFSM fsm = new MailboxFSM();	
		
		//STEP 3: DO THE FOLLOWING AFTER POSTING TO THE QUEUE- THIS WILL SET THE STATE OF THE EXECUTION TO QUEUED
        fsm.addState(stateDefination.getExecutionId() , stateDefination);
        
        //AFTER CONSUMING FROM THE QUEUE
        //DO STEP 1 - BUT THIS TIME YOU HAVE TO SET ALL THE FILEDS IN THE stateDefination SINCE YOU WILL HAVE ALL THE PROCESSOR DETAILS IN HAND
        //DO STEP 2
        //SKIP STEP 3
        //STEP 4:SET TRANSITION RULES
        fsm.addTransitionRules(stateDefination);
        
        //STEP5:TRIGGER PROCESSING EVENT
        fsm.handleEvent( new ActiveEvent<ExecutionEvents>( ExecutionEvents.PROCESSOR_EXECUTION_STARTED ) );
        Thread.sleep(5000);//ACTUAL PROCESSOR WORK WILL HAPPEN HERE
        //STEP 6: TRIGGER EVENT FOR SUCCESS OR FAILURE BASED ON PROCESSING RESULT
        fsm.handleEvent( new ActiveEvent<ExecutionEvents>( ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED ) );
        System.out.println("----------------------------------------------------------------------");
        
         
	}

}
