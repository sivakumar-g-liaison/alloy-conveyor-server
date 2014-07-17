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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.NamingException;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.com.liaison.queue.ProcessorQueue;



/**
 * 
 * @author OFS
 *
 */
public class HornetQMessageListnerTest {
     
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
           System.out.println("Done posting");		 
	 }
     
     /**
      * Method to test Thread.
      * 
      * @throws InterruptedException
      */
	/*@Test
	public void testThreading() throws InterruptedException {
		 
    	 MailboxProcessorQueueConsumer qconsumer = MailboxProcessorQueueConsumer.getMailboxProcessorQueueConsumerInstance();
			for(int i=0;i<10;i++){
				qconsumer.invokeProcessor("MyIdis"+i);
				
			};
			
			Thread.sleep(30000);
			System.out.println("Back from sleep");
			
			for(int i=0;i<10;i++){
				qconsumer.invokeProcessor("MyIdisNewId"+i);
				
			};
          while(true);	 
	 }*/
	 
	

}
