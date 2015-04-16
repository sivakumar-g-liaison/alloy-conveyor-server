/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.util.ConfigurationUtils;

/**
 *
 * @author OFS
 *
 */
public class EmailNotifier {

	protected static final Logger LOGGER = LogManager.getLogger(EmailNotifier.class);

	public static final String NOTE = "This message is automatically generated by the system.";
	public static final String SUBJECT = "Processor Failure Notifcation.";

	private static Properties MAILSERVER_CONFIG = new Properties();
	private static Session mailSession = null;



	static {
		fetchConfig();
	}

	/**
	 * Open a specific text file containing mail server parameters, and populate a corresponding
	 * Properties object.
	 */
	private static void fetchConfig() {

		InputStream input = null;
		try {
			Object env = ConfigurationManager.getDeploymentContext().getDeploymentEnvironment();
			String propertyFileName = "g2mailboxservice-" + env + ".properties";
			MAILSERVER_CONFIG.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(propertyFileName));
		} catch (IOException ex) {
		    LOGGER.error("Cannot open and load mail server properties file.");
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
			    LOGGER.error("Cannot close mail server properties file.");
			}
		}
	}


	/**
	 * Send a single email.
	 */
	public void sendEmail(List<String> toEmailAddrList, String subject, String body, String type) {

		try {
		    Session session = getMailSession(ConfigurationUtils.getProperties(MailBoxUtil.getEnvironmentProperties()));
            MimeMessage message = new MimeMessage(session);

            // the "from" address may be set in code, or set in the
            // config file under "mail.from" ; here, the latter style is used
            message.addHeader("Content-Type", type);
            message.setFrom(new InternetAddress(MAILSERVER_CONFIG.getProperty("mail.from")));

            if ((toEmailAddrList != null) && (toEmailAddrList.size() > 0)) {
                for (String toEmailAddr : toEmailAddrList) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddr));
                }
            }
            message.setSubject(subject);
            final String completeMessage = new StringBuilder().append(body).append("\n\n").append(NOTE).toString();
            message.setText(completeMessage);
            Transport.send(message);

        } catch (MessagingException ex ) {
        	LOGGER.error("Cannot send email. " + ex);

        }
	}

	
    private Session getMailSession(Properties sessionProperties){
        if(mailSession == null){
            mailSession = Session.getInstance(sessionProperties);
        }
        return mailSession;
    }
}