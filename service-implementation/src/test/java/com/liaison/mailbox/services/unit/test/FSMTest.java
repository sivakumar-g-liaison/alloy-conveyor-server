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

import com.liaison.mailbox.enums.ExecutionEvents;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.fsm.MailboxFSM;
import com.liaison.mailbox.service.core.fsm.ProcessorStateDTO;
import com.liaison.mailbox.service.exception.MailBoxFSMSetupException;

/**
 * 
 * 
 * @author veerasamyn
 */
public class FSMTest extends BaseServiceTest {

	
    /**
     * Method to test FSM state.
     * 
     * @throws InterruptedException
     */
	
	public void testFSM() throws InterruptedException{
        //STEP 1: BUILD THIS AS SOON AS YOU GET A PROCESSOR FROM DB AFTER TRIGGERING A PROFILE. FILL AS MUCH AS YOU CAN IN stateDefination BUT MUST HAVE EXECUTION ID,PROCESSOR ID and PROFILE NAME 
		ProcessorStateDTO initialProcessorState = new ProcessorStateDTO("GENERATED_EXECUTION_UDID_1","PROCESSOR_ID_FROM_DB",ExecutionState.QUEUED);
		initialProcessorState.setProfileName("DUMMY PROFILE NAME");	
		//STEP 2: INITAIALZE FSM INSTANCE		
		MailboxFSM fsm = new MailboxFSM();	
		
		//STEP 3: DO THE FOLLOWING AFTER POSTING TO THE QUEUE- THIS WILL SET THE STATE OF THE EXECUTION TO QUEUED
		System.out.println("POSTING TO THE QUEUE...");
        fsm.addState(initialProcessorState);
        
        //AFTER CONSUMING FROM THE QUEUE
        //DO STEP 1 - BUT THIS TIME YOU HAVE TO SET ALL THE FILEDS IN THE stateDefination SINCE YOU WILL HAVE ALL THE PROCESSOR DETAILS IN HAND
        //DO STEP 2
        //SKIP STEP 3
        //STEP 4:SET TRANSITION RULES
        try {
			fsm.addDefaultStateTransitionRules(initialProcessorState);
		} catch (MailBoxFSMSetupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //STEP5:TRIGGER PROCESSING EVENT
        System.out.println("PICKED UP FROM THE QUEUE.");
        fsm.handleEvent(fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_STARTED ) );
        System.out.println("PROCESSOR RUNNING...");//ACTUAL PROCESSOR WORK WILL HAPPEN HERE
        //STEP 6: TRIGGER EVENT FOR SUCCESS OR FAILURE BASED ON PROCESSING RESULT
        fsm.handleEvent( fsm.createEvent(ExecutionEvents.PROCESSOR_EXECUTION_COMPLETED ) );
        
        System.out.println("----------------------------------------------------------------------");
        
         
	}

}
