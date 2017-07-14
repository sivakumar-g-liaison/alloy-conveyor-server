/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dropbox;

import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAOBase;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.email.EmailInfoDTO;
import com.liaison.mailbox.service.core.email.EmailNotifier;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TenancyKeyUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * base services for conveyor use cases
 */
public class DropboxBaseService {

    private static final Logger LOGGER = LogManager.getLogger(DropboxStagedFilesService.class);
    private static String NEW_LINE = "\n\n";
    private static final String CONVEYOR = "Liaison Conveyor URL";
    private static String SEPARATOR = ": ";
    private static String PROPERTY_CONVEYOR_URL = "com.liaison.dropbox.conveyorUrl";

    /**
     * Validates the acl manifest and fetches the mailbox guids that matches with the tenancy key
     *
     * @param aclManifest manifest details with tenancy key
     * @return list of mailbox guid
     * @throws IOException
     */
    protected List<String> validateAndGetMailboxes(String aclManifest) throws IOException {

        LOGGER.debug("Retrieving tenancy keys from acl-manifest");
        // retrieve the tenancy key from acl manifest
        List<String> tenancyKeys = TenancyKeyUtil.getTenancyKeyGuids(aclManifest);
        if (tenancyKeys.isEmpty()) {
            LOGGER.error("Retrieval of tenancy key from acl manifest failed");
            throw new MailBoxServicesException(Messages.TENANCY_KEY_RETRIEVAL_FAILED, Response.Status.BAD_REQUEST);
        }

        LOGGER.debug("The retrieved tenancykey values are {}", tenancyKeys);

        // retrieve corresponding mailboxes of the available tenancyKeys.
        MailBoxConfigurationDAO mailboxDao = new MailBoxConfigurationDAOBase();
        LOGGER.debug("retrieve all mailboxes linked to tenancykeys {}", tenancyKeys);
        List<String> mailboxIds = mailboxDao.findAllMailboxesLinkedToTenancyKeys(tenancyKeys);

        if (mailboxIds.isEmpty()) {
            LOGGER.error("There are no mailboxes linked to the tenancyKeys");
            throw new MailBoxServicesException("There are no mailboxes available for tenancykeys",
                    Response.Status.NOT_FOUND);
        }
        return mailboxIds;
    }

    /**
     * Method to send email once file is staged successfully
     *
     * @param mailbox      - mailbox
     * @param emailSubject - email subject
     * @param emailBody    - email Body
     */
    protected void sendEmail(MailBox mailbox, String emailSubject, String emailBody) {

        List<String> emailAddressList = mailbox.getEmailAddress();
        if (null == emailAddressList || emailAddressList.isEmpty()) {
            LOGGER.info("Email address is not configured in the mailbox - {}", mailbox.getMbxName());
            return;
        }

        EmailInfoDTO emailInfo = new EmailInfoDTO(mailbox.getMbxName(), null, null, emailAddressList, emailSubject, emailBody, true, true);
        EmailNotifier.sendEmail(emailInfo);
    }

    /**
     * constructs the email body for the file download
     *
     * @param fileName file name
     * @return email body with details of the file staged
     */
    protected String constructEmailBody(String fileName) {

        StringBuilder emailContentBuilder = new StringBuilder()
                .append("Please login to Liaison Conveyor to download your file");
        if (!MailBoxUtil.isEmpty(fileName)) {
            emailContentBuilder.append(" '").append(fileName).append("'");
        }
        emailContentBuilder.append(NEW_LINE)
                .append(CONVEYOR)
                .append(SEPARATOR)
                .append(MailBoxUtil.getEnvironmentProperties().getString(PROPERTY_CONVEYOR_URL));
        return emailContentBuilder.toString();

    }
}
