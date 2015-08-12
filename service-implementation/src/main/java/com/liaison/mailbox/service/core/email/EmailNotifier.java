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
import java.net.InetAddress;
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

	private static String SEPARATOR = ": ";
	private static String MAILBOX_NAME = "Mailbox Name";
	private static String MAILBOX_ID = "Mailbox Id";
	private static String PROCESSOR_NAME = "Processor Name";
	private static String HOST_NAME = "Host Name";
	private static String ENVIRONMENT = "Environment";
	private static String FAILURE_REASON = "Failure Reason";
	private static String NEW_LINE = "\n\n";

	static {
		fetchConfig();
	}

	/**
	 * Open a specific text file containing mail server parameters, and populate a corresponding Properties object.
	 */
	private static void fetchConfig() {

		InputStream input = null;
		try {
			Object env = ConfigurationManager.getDeploymentContext().getDeploymentEnvironment();
			String propertyFileName = "g2mailboxservice-" + env + ".properties";
			MAILSERVER_CONFIG.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFileName));
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
	public void sendEmail(EmailInfoDTO emailInfo) {

		try {
			Session session = getMailSession(ConfigurationUtils.getProperties(MailBoxUtil.getEnvironmentProperties()));
			MimeMessage message = new MimeMessage(session);

			// the "from" address may be set in code, or set in the
			// config file under "mail.from" ; here, the latter style is used
			message.addHeader("Content-Type", emailInfo.getType());
			message.setFrom(new InternetAddress(MAILSERVER_CONFIG.getProperty("mail.from")));

			List<String> toEmailAddrList = emailInfo.getToEmailAddrList();
			if ((toEmailAddrList != null) && (toEmailAddrList.size() > 0)) {
				for (String toEmailAddr : toEmailAddrList) {
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddr));
				}
			}
			message.setSubject(emailInfo.getSubject());
			StringBuilder emailContentBuilder = new StringBuilder()
												.append(getEmailBodyPrefix(emailInfo))
												.append(emailInfo.getEmailBody())
												.append(NEW_LINE)
												.append(NOTE);
			final String completeMessage = emailContentBuilder.toString();
			message.setText(completeMessage);
			Transport.send(message);

		} catch (MessagingException ex) {
			LOGGER.error("Cannot send email. " + ex);

		}
	}


	private Session getMailSession(Properties sessionProperties) {
		if (mailSession == null) {
			mailSession = Session.getInstance(sessionProperties);
		}
		return mailSession;
	}


	/**
	 * Method to construct the email body prefix - Prefix will contain details like MailboxName, Id, Processor Name
	 * environment and host details
	 *
	 * @return email body prefix
	 */
	private String getEmailBodyPrefix(EmailInfoDTO emailInfo) {

		// get host name
		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			LOGGER.error("Unknown Host while constructing subject of email", e);
		}
		StringBuilder builder = new StringBuilder();

		// set mailbox name in email body if mailbox name is available
		if (!MailBoxUtil.isEmpty(emailInfo.getMailboxName())) {
			builder.append(MAILBOX_NAME).append(SEPARATOR).append(emailInfo.getMailboxName()).append(NEW_LINE);
		}

		// set mailbox id in email body if mailbox id is available
		if (!MailBoxUtil.isEmpty(emailInfo.getMailboxId()) && !emailInfo.isSuccess()) {
			builder.append(MAILBOX_ID).append(SEPARATOR).append(emailInfo.getMailboxId()).append(NEW_LINE);
		}

		// set processor name in email body if processor name is available
		if (!MailBoxUtil.isEmpty(emailInfo.getProcessorName()) && !emailInfo.isSuccess()) {
			builder.append(PROCESSOR_NAME).append(SEPARATOR).append(emailInfo.getProcessorName()).append(NEW_LINE);
		}

		// append environment details
		builder.append(ENVIRONMENT).append(SEPARATOR).append(
				ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()).append(NEW_LINE);

		// set host name in email body if host name is available
		if (!MailBoxUtil.isEmpty(hostName) && !emailInfo.isSuccess()) {
			builder.append(HOST_NAME).append(SEPARATOR).append(hostName).append(NEW_LINE);
		}

		// append the failure reason
		if (!emailInfo.isSuccess()) {
			builder.append(FAILURE_REASON).append(SEPARATOR).append(NEW_LINE);
		}

		return builder.toString();
	}
}