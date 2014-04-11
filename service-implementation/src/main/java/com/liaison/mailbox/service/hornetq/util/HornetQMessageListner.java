/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.hornetq.util;

import java.io.IOException;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.liaison.mailbox.service.core.processor.MailboxProcessorQueueConsumer;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class HornetQMessageListner implements MessageListener {
	
	private static final Logger logger = LogManager.getLogger(HornetQMessageListner.class);

	public HornetQMessageListner(final ConnectionFactory cf,
			final Destination destination) throws JMSException {
		// create a JMS connection
		javax.jms.Connection connection = cf.createConnection();
		//connection.setClientID("mailboxProcessorsQueue-listner");
		// create a JMS session
		Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

		// create a JMS MessageConsumer to consume message from the destination
		MessageConsumer consumer = session.createConsumer(destination);
		consumer.setMessageListener(this);

		// start the connection to start consuming messages
		connection.start();
		logger.info("Listner Started successfully");
	}

	@Override
	public void onMessage(Message message) {

		try {
			TextMessage textMessage = (TextMessage) message;
			 MailboxProcessorQueueConsumer qconsumer = MailboxProcessorQueueConsumer.getMailboxProcessorQueueConsumerInstance();
			 qconsumer.invokeProcessor(textMessage.getText());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void init() throws NamingException, JMSException, IOException {	
		
		
		final Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		env.put(Context.PROVIDER_URL, MailBoxUtility.getEnvironmentProperties().getProperty("providerurl"));
		//env.put(Context.SECURITY_PRINCIPAL, "guest");
		//env.put(Context.SECURITY_CREDENTIALS, "pass");
		Context context = new InitialContext(env);
		ConnectionFactory cf = (ConnectionFactory) context.lookup(MailBoxUtility.getEnvironmentProperties().getString("queueConnectionFactory"));
		Destination destination = (Destination) context.lookup(MailBoxUtility.getEnvironmentProperties().getString("mailBoxProcessorQueue"));
		new HornetQMessageListner(cf, destination);
		logger.info("Starting up the JMS listner");

	}

}
