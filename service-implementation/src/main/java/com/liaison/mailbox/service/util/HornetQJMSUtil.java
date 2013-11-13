package com.liaison.mailbox.service.util;

import java.util.Hashtable;

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

import com.liaison.mailbox.service.dto.ConfigureJNDIDTO;

public class HornetQJMSUtil {
	
	 public static final String  QUEUECONFAC = 
             "/ConnectionFactory";

	 private static Context jndiContext = null;
	 
	 private static String g2QueueName = "/queue/G2Queue";
	 
	 private static Hashtable<String, String> jndiProperties = new Hashtable<String,String>();
	 
	 
	 public static void initializeConext(ConfigureJNDIDTO jndidto){
		 jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, jndidto.getInitialContextFactory());
		 jndiProperties.put(Context.PROVIDER_URL, jndidto.getProviderURL());
		 jndiProperties.put(Context.URL_PKG_PREFIXES, jndidto.getUrlPackagePrefixes());
	 }
	 
	 /**
	     * Returns a QueueConnectionFactory object.
	     *
	     * @return    a QueueConnectionFactory object
	     * @throws    javax.naming.NamingException (or other
	     *            exception) if name cannot be found
	     */
	    public static ConnectionFactory
	            getQueueConnectionFactory() throws Exception {
	        return (ConnectionFactory) jndiLookup(QUEUECONFAC);
	    }
	    
	    /**
	     * Returns a Queue object.
	     *
	     * @param name       String specifying queue name
	     * @param session    a QueueSession object
	     *
	     * @return           a Queue object
	     * @throws           javax.naming.NamingException (or other
	     *                   exception) if name cannot be found
	     */
	    public static Queue getQueue(String name) throws Exception {
	        return (Queue) jndiLookup(name);
	    }
	    
	    /**
	     * Creates a JNDI API InitialContext object if none exists
	     * yet. Then looks up the string argument and returns the
	     * associated object.
	     *
	     * @param name    the name of the object to be looked up
	     *
	     * @return        the object bound to name
	     * @throws        javax.naming.NamingException (or other
	     *                exception) if name cannot be found
	     */
	    public static Object jndiLookup(String name) 
	        throws NamingException {
	        Object    obj = null;

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
	     * Sends a message to g2Queue.  Called by a subscriber
	     * to notify a publisher that it is ready to receive
	     * messages.
	     * <p>
	     * If g2Queue doesn't exist, the method throws an 
	     * exception.
	     *

	     * @param stringMessage to be posted to the queue
	     */
	    public static void sendMessage(String stringMessage) 
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
	            g2Queue = getQueue(g2QueueName);
	        } catch (Exception e) {
	            System.err.println("Connection problem: " + 
	                e.toString());
	            if (queueConnection != null) {
	                try {
	                    queueConnection.close();
	                } catch (JMSException ee) {}
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
	                } catch (JMSException e) {}
	            }
	        }
	    }
	    
	    /**
	     * retrieves the message from the queue
	     */
	    public static void receiveMessage() 
	            throws Exception {
	        ConnectionFactory  queueConnectionFactory = null;
	        Connection         queueConnection = null;
	        Session            queueSession = null;
	        Queue              g2Queue = null;
	        MessageConsumer    messageConsumer = null;

	        try {
	            queueConnectionFactory = 
	                getQueueConnectionFactory();
	            queueConnection = 
	                queueConnectionFactory.createConnection();
	            queueSession = 
	                queueConnection.createSession(false, 
	                    Session.AUTO_ACKNOWLEDGE);
	            g2Queue = getQueue(g2QueueName);
	            queueConnection.start();
	        } catch (Exception e) {
	            System.err.println("Connection problem: " + 
	                e.toString());
	            if (queueConnection != null) {
	                try {
	                    queueConnection.close();
	                } catch (JMSException ee) {}
	            }
	            throw e;
	        } 

	        try {
	           
	            messageConsumer = 
	                queueSession.createConsumer(g2Queue);
	            TextMessage message = (TextMessage)messageConsumer.receive();
	            System.out.println("Received message:"+message.getText());
	        } catch (JMSException e) {
	            System.err.println("Exception occurred: " + 
	                e.toString());
	            throw e;
	        } finally {
	            if (queueConnection != null) {
	                try {
	                    queueConnection.close();
	                } catch (JMSException e) {}
	            }
	        }
	    }

	public static void main(String[] args) throws Exception{
		
		ConfigureJNDIDTO  jndidto = new ConfigureJNDIDTO();
		jndidto.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
		jndidto.setProviderURL("jnp://10.0.24.76:1099");
		jndidto.setUrlPackagePrefixes("org.jboss.naming");
		
		initializeConext(jndidto);
		sendMessage("test message posted to Queue");
		receiveMessage();
	}

}
