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

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.core.email.EmailInfoDTO;
import com.liaison.mailbox.service.core.email.EmailNotifier;

/**
 * @author VNagarajan
 *
 */
public class EmailUtil {

    private static final Logger LOGGER = LogManager.getLogger(EmailUtil.class);

    /**
     * Method to send emails according to the details provided in the email helper dto.
     *
     * @param emailInfoDTO - EmailHelper DTO which contain all email details
     */
    public static void sendEmail(Processor processor, String emailSubject, Exception e) {
        sendEmail(processor, emailSubject, ExceptionUtils.getStackTrace(e));
    }

    /**
     * Method to send emails according to the details provided in the email helper dto.
     *
     * @param emailInfoDTO - EmailHelper DTO which contain all email details
     */
    public static void sendEmail(Processor processor, String emailSubject, String failureReason) {

        if (processor == null) {
            return;
        }

        List<String> emailAddress = processor.getEmailAddress();
        if (null == emailAddress || emailAddress.isEmpty()) {
            LOGGER.info("Email Address is not configured.");
            return;
        }

        EmailInfoDTO emailInfoDTO = new EmailInfoDTO();
        emailInfoDTO.copyToDTO(processor, emailAddress, emailSubject, failureReason);
        LOGGER.info("Ready to send email to {}", emailAddress);
        EmailNotifier emailNotifier = new EmailNotifier();
        emailNotifier.sendEmail(emailInfoDTO);
        LOGGER.info("Email sent successfully to {}", emailInfoDTO.getToEmailAddrList());
    }

    /**
     * Sent notifications for trigger system failure.
     * Method to construct subject based on details of processor
     *
     * @param processor - The processor for which execution fails
     * @return email Subject as String
     */
    public static String constructSubject(Processor processor) {

        LOGGER.debug("constructing subject for the mail to be sent for processor execution failure");
        StringBuilder subjectBuilder = null;
        if (null == processor) {
            subjectBuilder = new StringBuilder()
                .append("Processor execution failed");
        } else {
            subjectBuilder = new StringBuilder()
                    .append("Processor:")
                    .append(processor.getProcsrName())
                    .append(" execution failed for the mailbox ")
                    .append(processor.getMailbox().getMbxName())
                    .append("(")
                    .append(processor.getMailbox().getPguid())
                    .append(")");
        }
        return subjectBuilder.toString();
    }

}
