package com.liaison.mailbox.services.unit.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.testng.annotations.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.core.processor.MailboxProcessorQueueConsumer;
import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;
import com.liaison.mailbox.service.util.HornetQJMSUtil;




public class HornetQMessageListnerTest {
     @Test
	 public void postToQueue() throws NamingException, JMSException,Exception{
		 
		 HornetQJMSUtil util = new HornetQJMSUtil();
		    String propertyFileName = "g2mailboxservice-dev.properties";
			String props = ServiceUtils.readFileFromClassPath(propertyFileName);
			InputStream is = new ByteArrayInputStream(props.getBytes("UTF-8"));
			Properties properties = new Properties();
			properties.load(is);
			String providerURL = properties.getProperty("providerurl");
			String queueName =properties.getProperty("mailBoxProcessorQueue");

			ConfigureJNDIDTO jndidto = new ConfigureJNDIDTO();
			jndidto.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
			jndidto.setProviderURL(providerURL);
			jndidto.setQueueName(queueName);
			jndidto.setUrlPackagePrefixes("org.jboss.naming");
			for(int i=0;i<2;i++){
			jndidto.setMessage("mynewID"+i);
			util.postMessage(jndidto);
			};
           System.out.println("Done posting");		 
	 }
     
     @Test
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
	 }
	 
	

}
