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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.MailSend;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.netflix.config.ConfigurationManager;

/**
 * This will handles the email notification operations.
 * 
 * @author OFS
 */
public class EmailNotifier {

	protected static final Logger LOGGER = LogManager.getLogger(EmailNotifier.class);
	private static final DecryptableConfiguration PROPS = MailBoxUtil.getEnvironmentProperties();

	public static final String NOTE = "This message is automatically generated by the system.";
	public static final String SUBJECT = "Processor Failure Notifcation.";

	private static String SEPARATOR = ": ";
	private static String MAILBOX_NAME = "Mailbox Name";
	private static String MAILBOX_ID = "Mailbox Id";
	private static String PROCESSOR_NAME = "Processor Name";
	private static String HOST_NAME = "Host Name";
	private static String ENVIRONMENT = "Environment";
	private static String FAILURE_REASON = "Failure Reason";
	private static String NEW_LINE = "\n\n";

	/**
	 * Send a single email.
	 * 
	 * @param strFrom
	 * @param strTo
	 * @param strSubject
	 * @param aBody
	 * @param resetStatus
	 * @param containPassword
	 */
	public static void sendEmail(EmailInfoDTO emailInfo) {

		try {

			StringBuilder emailContentBuilder = new StringBuilder()
					.append(getEmailBodyPrefix(emailInfo))
					.append(emailInfo.getEmailBody())
					.append(NEW_LINE)
					.append(NOTE);

			if (!emailInfo.getToEmailAddrList().isEmpty()) {
				MailSend.Send(StringUtils.join(emailInfo.getToEmailAddrList(), ','),
						PROPS.getString("mail.from"),
						emailInfo.getSubject(),
						emailContentBuilder.toString(),
						PROPS.getString("mail.host"));
			}

		} catch (Exception e) {
			//hanlde gracefully
			LOGGER.error("Cannot send email. " + e);
		}

	}

	/**
	 * Method to construct the email body prefix - Prefix will contain details like MailboxName, Id, Processor Name
	 * environment and host details
	 *
	 * @return email body prefix
	 */
	private static String getEmailBodyPrefix(EmailInfoDTO emailInfo) {

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
		if (!emailInfo.isDropbox()) {
			builder.append(ENVIRONMENT).append(SEPARATOR).append(
					ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()).append(NEW_LINE);
		}

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

	/**
     * Method to send emails according to the details provided in the email helper dto.
     *
     * @param emailInfoDTO - EmailHelper DTO which contain all email details
     */
    public static void sendEmail(Processor processor, String emailSubject, Exception e) {
        sendEmail(processor, emailSubject, ExceptionUtils.getStackTrace(e), false);
    }

    /**
     * Method to send emails according to the details provided in the email helper dto.
     *
     * @param emailInfoDTO - EmailHelper DTO which contain all email details
     */
    public static void sendEmail(Processor processor, String emailSubject, String emailBody, boolean isSuccess) {

        if (processor == null) {
            return;
        }

        List<String> emailAddress = new ArrayList<>();
        if (isSuccess) {
           emailAddress = processor.getEmailAddress();
        } else {
        	String[] internalEmail = (MailBoxUtil.getEnvironmentProperties().getStringArray(MailBoxConstants.ERROR_RECEIVER));
        	// Validate the email address configuration from Property file
        	for (String tempEmail : internalEmail) {
        		if (!MailBoxUtil.isEmpty(tempEmail)) {
        			emailAddress.add(tempEmail);
        		}        		
        	}        	
        }

        if (null == emailAddress || emailAddress.isEmpty()) {
            LOGGER.info("Email Address is not configured.");
            return;
        }

        EmailInfoDTO emailInfoDTO = new EmailInfoDTO();
        emailInfoDTO.copyToDTO(processor, emailAddress, emailSubject, emailBody, isSuccess);
        LOGGER.info("Ready to send email to {}", emailAddress);

        EmailNotifier.sendEmail(emailInfoDTO);
        LOGGER.info("Email sent successfully to {}", emailInfoDTO.getToEmailAddrList());
    }


    /**
     * Sent notifications for trigger system failure.
     * Method to construct subject based on details of processor
     *
     * @param processor - The processor for which execution fails
     * @return email Subject as String
     */
    public static String constructSubject(Processor processor, boolean isStagingSuccess) {

        LOGGER.debug("constructing subject for the mail to be sent for processor execution failure");
        StringBuilder subjectBuilder = null;
        if (null == processor) {
            subjectBuilder = new StringBuilder().append("Processor execution failed");
        } else {
            subjectBuilder = new StringBuilder();
            if (!isStagingSuccess) {
            	subjectBuilder.append("Processor:")
                  	.append(processor.getProcsrName())
                  	.append(" execution failed for the mailbox ")
                  	.append(processor.getMailbox().getMbxName())
                  	.append("(")
                  	.append(processor.getMailbox().getPguid())
                  	.append(")");
            } else {
            	subjectBuilder.append("File is available for pick up for the processor :")
                	.append(processor.getProcsrName());
            }

        }
        return subjectBuilder.toString();
    }

}