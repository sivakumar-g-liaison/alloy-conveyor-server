/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.liaison.mailbox.service.queue.kafka.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.StringUtil;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import com.liaison.mailbox.service.queue.sender.SweeperQueueSendClient;

import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_QUEUE_SERVICE_ENABLED;
import static com.liaison.mailbox.MailBoxConstants.SERVICE_BROKER_APP_ID;
import static com.liaison.mailbox.MailBoxConstants.WORK_TICKET_QUEUE_TOPIC_SUFFIX;
import static com.liaison.mailbox.service.util.MailBoxUtil.CONFIGURATION;

/**
 * Utilities for WorkTicket.
 *
 * @author OFS
 */
public class WorkTicketUtil {

    private static final Logger LOGGER = LogManager.getLogger(WorkTicketUtil.class);
    private static final int GLOBAL_PROCESS_ID_MIN_LENGTH = 3;
    private static final int GLOBAL_PROCESS_ID_MAX_LENGTH = 32;
    private static final String GLOBAL_PROCESS_ID_PATTERN = "^[a-zA-Z0-9]*$";

    /**
     * retrieve the pipeline id configured in httplistener of mailbox
     *
     * @param httpListenerProperties http processor properties
     * @return String pipeline id
     */
    public static String retrievePipelineId(Map<String, String> httpListenerProperties) {
        return httpListenerProperties.get(MailBoxConstants.PROPERTY_HTTPLISTENER_PIPELINEID);
    }

    public static void postWrkTcktToQ(WorkTicket workTicket) throws Exception {
        String workTicketJson = JAXBUtility.marshalToJSON(workTicket);
        postToQueue(workTicketJson);
    }

    private static void postToQueue(String message) throws Exception {
        try {
            if (CONFIGURATION.getBoolean(CONFIGURATION_QUEUE_SERVICE_ENABLED, false)) {
                // Only WorkTickets handled
                Producer.produceWorkTicketToQS(getWorkTicketFromMessageText(message),
                        SERVICE_BROKER_APP_ID, WORK_TICKET_QUEUE_TOPIC_SUFFIX);
            } else {
                SweeperQueueSendClient.getInstance().sendMessage(message);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOGGER.debug("postToQueue, message: {}", message);
    }

    private static WorkTicket getWorkTicketFromMessageText(String messageText) {
        try {
            return JAXBUtility.unmarshalFromJSON(messageText, WorkTicket.class);
        } catch (Exception e) {
            String errorMessage = "Relay: Error while parsing WorkTicket JSON. Input JSON data: " + messageText;
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Validates the global process id if it is available in the header and set in the workticket. Generates new guid if
     * it is not available
     *
     * @param workTicket work ticket dto
     * @param globalProcessId - got from request header.
     */
    public static void setGlobalProcessId(WorkTicket workTicket, String globalProcessId) {

        if (!StringUtil.isNullOrEmptyAfterTrim(globalProcessId)) {

            if (GLOBAL_PROCESS_ID_MAX_LENGTH < globalProcessId.length()
                    || GLOBAL_PROCESS_ID_MIN_LENGTH > globalProcessId.length()
                    || !(globalProcessId.matches(GLOBAL_PROCESS_ID_PATTERN))) {
                throw new MailBoxServicesException("The global process id is invalid", Response.Status.BAD_REQUEST);
            } else {
                workTicket.setGlobalProcessId(globalProcessId);

            }

        } else {
            workTicket.setGlobalProcessId(MailBoxUtil.getGUID());
        }

        LOGGER.debug("The GPID set in workticket is {}", workTicket.getGlobalProcessId());

    }

    /**
     * This method will create workTicket by given request.
     *
     * @param requestProp http request properties
     * @param requestHeaders http request headers
     * @param httpListenerProperties processor properties
     * @return WorkTicket
     */
    public static WorkTicket createWorkTicket(Map<String, Object> requestProp,
                                              Map<String, List<String>> requestHeaders,
                                              Map<String, String> httpListenerProperties) {

        WorkTicket workTicket = new WorkTicket();
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_METHOD, requestProp.get(MailBoxConstants.HTTP_METHOD));
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_QUERY_STRING,
                requestProp.get(MailBoxConstants.HTTP_QUERY_STRING));
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_REMOTE_PORT,
                requestProp.get(MailBoxConstants.HTTP_REMOTE_PORT));
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_CHARACTER_ENCODING,
                requestProp.get(MailBoxConstants.HTTP_CHARACTER_ENCODING));
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_REMOTE_USER,
                requestProp.get(MailBoxConstants.HTTP_REMOTE_USER));
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_REMOTE_ADDRESS,
                requestProp.get(MailBoxConstants.HTTP_REMOTE_ADDRESS));
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_REQUEST_PATH,
                requestProp.get(MailBoxConstants.HTTP_REQUEST_PATH));
        workTicket.setAdditionalContext(MailBoxConstants.HTTP_CONTENT_TYPE,
                requestProp.get(MailBoxConstants.HTTP_CONTENT_TYPE));
        workTicket.getAdditionalContext().putAll(requestHeaders);
        workTicket.getHeaders().putAll(requestHeaders);
        workTicket.setCreatedTime(new Date());

        if (null != httpListenerProperties) {
            workTicket.setPipelineId(WorkTicketUtil.retrievePipelineId(httpListenerProperties));
            workTicket.setAdditionalContext(MailBoxConstants.KEY_MAILBOX_ID,
                    httpListenerProperties.get(MailBoxConstants.KEY_MAILBOX_ID));
            workTicket.setAdditionalContext(MailBoxConstants.KEY_MAILBOX_NAME,
                    httpListenerProperties.get(MailBoxConstants.KEY_MAILBOX_NAME));
        }
        setGlobalProcessId(workTicket, (String) requestProp.get(MailBoxConstants.GLOBAL_PROCESS_ID_HEADER));

        return workTicket;
    }

}
