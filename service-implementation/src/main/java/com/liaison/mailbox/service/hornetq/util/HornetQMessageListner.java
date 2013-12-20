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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.service.core.processor.MailboxProcessorQueueConsumer;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class HornetQMessageListner implements MessageListener {
	
	private static final Logger logger = LoggerFactory.getLogger(HornetQMessageListner.class);

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
		ConnectionFactory cf = (ConnectionFactory) context.lookup(MailBoxUtility.getEnvironmentProperties().getProperty("queueConnectionFactory"));
		Destination destination = (Destination) context.lookup(MailBoxUtility.getEnvironmentProperties().getProperty("mailBoxProcessorQueue"));
		new HornetQMessageListner(cf, destination);
		logger.info("Starting up the JMS listner");

	}

}
