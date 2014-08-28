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

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.com.liaison.queue.ProcessorQueue;



/**
 * 
 * @author OFS
 *
 */
public class HornetQMessageListnerTest {
     
	
	private static final Logger logger = LogManager.getLogger(HornetQMessageListnerTest.class);
	/**
	 * Method construts ConfigureJNDIDTO.
	 * 
	 * @throws NamingException
	 * @throws JMSException
	 * @throws Exception
	 */
	 public void postToQueue() throws NamingException, JMSException,Exception{

		for(int i=0;i<2;i++){
            ProcessorQueue.getInstance().pushMessages("mynewID"+i);
		}
		logger.debug("Done posting");		 
	 }	

}
