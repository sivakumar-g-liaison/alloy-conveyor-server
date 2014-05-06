/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.util.Hashtable;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;

/**
 * 
 * @author OFS
 *
 */
public class HornetQJMSUtil {
    
	private static final Logger logger = LoggerFactory.getLogger(HornetQJMSUtil.class);
	public static final String QUEUECONFAC =
			"/ConnectionFactory";

	private static Context jndiContext = null;

	private static String g2QueueName = "/queue/G2Queue";

	private static Hashtable<String, String> jndiProperties = new Hashtable<String, String>();

	private static void initializeConext(ConfigureJNDIDTO jndidto) {
		jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, jndidto.getInitialContextFactory());
		jndiProperties.put(Context.PROVIDER_URL, jndidto.getProviderURL());
		jndiProperties.put(Context.URL_PKG_PREFIXES, jndidto.getUrlPackagePrefixes());
	}

	/**
	 * Returns a QueueConnectionFactory object.
	 * 
	 * @return a QueueConnectionFactory object
	 * @throws javax.naming.NamingException
	 *             (or other exception) if name cannot be found
	 */
	private static ConnectionFactory
			getQueueConnectionFactory() throws Exception {
		return (ConnectionFactory) jndiLookup(QUEUECONFAC);
	}

	/**
	 * Returns a Queue object.
	 * 
	 * @param name
	 *            String specifying queue name
	 * @param session
	 *            a QueueSession object
	 * 
	 * @return a Queue object
	 * @throws javax.naming.NamingException
	 *             (or other exception) if name cannot be found
	 */
	private static Queue getQueue(String name) throws Exception {
		return (Queue) jndiLookup(name);
	}

	/**
	 * Creates a JNDI API InitialContext object if none exists yet. Then looks up the string
	 * argument and returns the associated object.
	 * 
	 * @param name
	 *            the name of the object to be looked up
	 * 
	 * @return the object bound to name
	 * @throws javax.naming.NamingException
	 *             (or other exception) if name cannot be found
	 */
	private static Object jndiLookup(String name)
			throws NamingException {
		Object obj = null;

		if (jndiContext == null) {
			try {
				jndiContext = new InitialContext(jndiProperties);
			} catch (NamingException e) {
				System.err.println("Could not create JNDI API " +
						"context: " + e.toString());
				throw e;
			}
		}
		try {
			obj = jndiContext.lookup(name);
		} catch (NamingException e) {
			System.err.println("JNDI API lookup failed: " +
					e.toString());
			throw e;
		}
		return obj;
	}

	/**
	 * Sends a message to g2Queue. Called by a subscriber to notify a publisher that it is ready to
	 * receive messages.
	 * <p>
	 * If g2Queue doesn't exist, the method throws an exception.
	 * 
	 * 
	 * @param stringMessage
	 *            to be posted to the queue
	 */
	private static void sendMessage(String stringMessage, String queueName)
			throws Exception {
		ConnectionFactory queueConnectionFactory = null;
		Connection queueConnection = null;
		Session queueSession = null;
		Queue g2Queue = null;
		MessageProducer messageProducer = null;
		TextMessage message = null;

		try {
			queueConnectionFactory =
					getQueueConnectionFactory();
			queueConnection =
					queueConnectionFactory.createConnection();
			queueSession =
					queueConnection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
			g2Queue = getQueue(queueName);
		} catch (Exception e) {
			System.err.println("Connection problem: " +
					e.toString());
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException ee) {
					logger.error("Could not close the connection while sends a message to g2Queue ." +ee);
				}
			}
			throw e;
		}

		try {
			messageProducer =
					queueSession.createProducer(g2Queue);
			message = queueSession.createTextMessage();
			message.setText(stringMessage);
			messageProducer.send(message);
		} catch (JMSException e) {
			System.err.println("Exception occurred: " +
					e.toString());
			throw e;
		} finally {
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException e) {
					logger.error("Could not close the connection while sends a message ." +e);
				}
			}
		}
	}
	
	/**
	 * Sends a message to g2Queue. Called by a subscriber to notify a publisher that it is ready to
	 * receive messages.
	 * <p>
	 * If g2Queue doesn't exist, the method throws an exception.
	 * 
	 * 
	 * @param stringMessage
	 *            to be posted to the queue
	 */
	private static void sendMessages(List<String> stringMessages, String queueName)
			throws Exception {
		ConnectionFactory queueConnectionFactory = null;
		Connection queueConnection = null;
		Session queueSession = null;
		Queue g2Queue = null;
		MessageProducer messageProducer = null;
		TextMessage message = null;

		try {
			queueConnectionFactory =
					getQueueConnectionFactory();
			queueConnection =
					queueConnectionFactory.createConnection();
			queueSession =
					queueConnection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
			g2Queue = getQueue(queueName);
		} catch (Exception e) {
			System.err.println("Connection problem: " +
					e.toString());
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException ee) {
					logger.error("Could not close the connection while sends a messages to g2Queue ." +ee);
				}
			}
			throw e;
		}

		try {
			messageProducer =
					queueSession.createProducer(g2Queue);
			message = queueSession.createTextMessage();
			
			if (stringMessages != null && !stringMessages.isEmpty()) {
				
				for (String msg : stringMessages) {
					message.setText(msg);
					messageProducer.send(message);
				}
			}
		} catch (JMSException e) {
			System.err.println("Exception occurred: " +
					e.toString());
			throw e;
		} finally {
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException e) {
					logger.error("Could not close the connection while sends a messages ." +e);
				}
			}
		}
	}

	/**
	 * retrieves the message from the queue
	 */
	private static void receiveMessage(String queueName)
			throws Exception {
		ConnectionFactory queueConnectionFactory = null;
		Connection queueConnection = null;
		Session queueSession = null;
		Queue g2Queue = null;
		MessageConsumer messageConsumer = null;

		try {
			queueConnectionFactory =
					getQueueConnectionFactory();
			queueConnection =
					queueConnectionFactory.createConnection();
			queueSession =
					queueConnection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
			g2Queue = getQueue(queueName);
			queueConnection.start();
		} catch (Exception e) {
			System.err.println("Connection problem: " +
					e.toString());
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException ee) {
					logger.error("Could not close the connection while receive a messages from g2Queue ." +ee);
				}
			}
			throw e;
		}

		try {

			messageConsumer =
					queueSession.createConsumer(g2Queue);
			TextMessage message = (TextMessage) messageConsumer.receive();
			System.out.println("Received message:" + message.getText());
		} catch (JMSException e) {
			System.err.println("Exception occurred: " + e.toString());
			throw e;
		} finally {
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException e) {
					logger.error("Could not close the connection while receive a messages ." +e);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {

		ConfigureJNDIDTO jndidto = new ConfigureJNDIDTO();
		jndidto.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
		jndidto.setProviderURL("jnp://localhost:1099");
		jndidto.setUrlPackagePrefixes("org.jboss.naming");

		initializeConext(jndidto);
		for (int i = 1; i < 500; i++)
		sendMessage("" + i, g2QueueName);
		Thread.sleep(1000*10);
		for (int i = 1; i < 500; i++)
		receiveMessage(g2QueueName);
	}

	/**
	 * Method send the message to queue. The queue name will be available in the ConfigureJNDIDTO.
	 * So, Util can post the message to different queue.
	 * 
	 * @param jndidto
	 *            The JNDI configuration DTO.
	 * @throws Exception
	 */
	public static void postMessage(ConfigureJNDIDTO jndidto) throws Exception {

		initializeConext(jndidto);
		sendMessage(jndidto.getMessage(), jndidto.getQueueName());
		// receiveMessage(jndidto.getQueueName());
	}
	
	/**
	 * Method send the messages to queue. The queue name will be available in the ConfigureJNDIDTO.
	 * So, Util can post the message to different queue.
	 * 
	 * @param jndidto
	 *            The JNDI configuration DTO.
	 * @throws Exception
	 */
	public static void postMessages(ConfigureJNDIDTO jndidto) throws Exception {

		initializeConext(jndidto);
		sendMessages(jndidto.getMessages(), jndidto.getQueueName());
		// receiveMessage(jndidto.getQueueName());
	}
}
