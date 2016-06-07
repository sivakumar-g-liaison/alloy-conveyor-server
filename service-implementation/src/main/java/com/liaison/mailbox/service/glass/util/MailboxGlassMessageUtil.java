/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.glass.util;

import java.util.Date;

import com.liaison.commons.message.glass.dom.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.ActivityStatusAPI;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.commons.message.glass.util.GlassMessageUtil;
import com.liaison.commons.util.UUIDGen;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Util for GlassMessage
 * 
 * @author VNagarajan
 *
 */
public class MailboxGlassMessageUtil {

    private static final String MAILBOX_ASA_IDENTIFIER = "MAILBOX";

    private static final Logger logger = LogManager.getLogger(MailboxGlassMessageUtil.class);
    
    /**
     * log TVAPI and Event logs
     * 
     * @param globalProcessId
     * @param processorType
     * @param processProtocol
     * @param fileName
     * @param filePath
     * @param fileLength
     * @param status
     * @param message
     * @param pipelineId
     */
    public static void logGlassMessage(final String globalProcessId,
            final ProcessorType processorType,
            final String processProtocol,
            final String fileName,
            final String filePath,
            final long fileLength,
            final ExecutionState status,
            final String message,
            final String pipelineId
            ) {
        
        MailboxGlassMessageUtil.logGlassMessage(
                globalProcessId,
                processorType,
                processProtocol,
                fileName,
                filePath,
                fileLength,
                status,
                message,
                pipelineId, 
                null);
    }

    /**
     * log TVAPI and Event logs
     * 
     * @param globalProcessId
     * @param processorType
     * @param processProtocol
     * @param fileName
     * @param filePath
     * @param fileLength
     * @param status
     * @param message
     */
    public static void logGlassMessage(final String globalProcessId,
            final ProcessorType processorType,
            final String processProtocol,
            final String fileName,
            final String filePath,
            final long fileLength,
            final ExecutionState status,
            final String message,
            final String pipelineId,
            final ExecutionTimestamp firstCornerTimeStamp) {

        TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
        GlassMessage glassMessage = new GlassMessage();
        glassMessage.setGlobalPId(globalProcessId);
        glassMessage.setCategory(processorType);
        glassMessage.setProtocol(processProtocol);
        glassMessage.setStatus(status);

        if (ExecutionState.COMPLETED.equals(status)) {
            glassMessage.setOutAgent(processProtocol);
            glassMessage.setOutSize(fileLength);

            // Fourth corner timestamp
            glassMessage.logFourthCornerTimestamp();
            logProcessingStatus(glassMessage, StatusType.SUCCESS, message);
        } else if (ExecutionState.FAILED.equals(status)) {
            logProcessingStatus(glassMessage, StatusType.ERROR, message);
        } else if (ExecutionState.DUPLICATE.equals(status)) {
            glassMessage.setOutAgent(processProtocol);
            glassMessage.setOutboundFileName(fileName);
            logProcessingStatus(glassMessage, StatusType.SUCCESS, message);
        } else if (ExecutionState.PROCESSING.equals(status)) {

            if (ProcessorType.SWEEPER.equals(processorType)) {
                glassMessage.setInAgent(filePath);
            } else {
                glassMessage.setInAgent(processProtocol);
            }
            glassMessage.setInboundFileName(fileName);
            glassMessage.setInboundPipelineId(pipelineId);
            glassMessage.setInSize(fileLength);

            // First corner timestamp
            if (null != firstCornerTimeStamp) {
                glassMessage.logFirstCornerTimestamp(firstCornerTimeStamp);
            }
            logProcessingStatus(glassMessage, StatusType.RUNNING, message);

            // Queued message for async inbound
            if (ProcessorType.HTTPASYNCPROCESSOR.equals(processorType)
                    || ProcessorType.SWEEPER.equals(processorType)) {
                logProcessingStatus(glassMessage, StatusType.QUEUED, "Workticket queued for file " + fileName);
            }
        } else if (ExecutionState.READY.equals(status)) {

            glassMessage.setOutAgent(processProtocol);
            glassMessage.setOutboundFileName(fileName);
            glassMessage.setOutboundPipelineId(pipelineId);
            glassMessage.setOutSize(fileLength);
        }

        // TVAPI
        transactionVisibilityClient.logToGlass(glassMessage);
    }

    public static GlassMessage getGlassMessage(WorkTicket wrkTicket, String processorProtocol, ProcessorType processorType) {

        GlassMessage glassMessage = new GlassMessage();
        glassMessage.setGlobalPId(wrkTicket.getGlobalProcessId());
        glassMessage.setOutboundPipelineId(wrkTicket.getPipelineId());
        glassMessage.setOutSize(wrkTicket.getPayloadSize());

        glassMessage.setTransferProfileName(wrkTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));
        glassMessage.setProcessorId(wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_PROCESSOR_ID));
        glassMessage.setTenancyKey(wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_WORKTICKET_TENANCYKEY));
        glassMessage.setTransferProfileName(wrkTicket.getAdditionalContextItem(MailBoxConstants.DBX_WORK_TICKET_PROFILE_NAME));
        glassMessage.setServiceInstandId(wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_SERVICE_INSTANCE_ID));
        glassMessage.setMailboxId(wrkTicket.getAdditionalContextItem(MailBoxConstants.KEY_MAILBOX_ID));

        glassMessage.setProtocol(processorProtocol);
        glassMessage.setCategory(processorType);

        return glassMessage;
    }

    public static void logProcessingStatus(GlassMessage glassMessage, StatusType statusType, String message) {

        // Log ActivityStatusAPI
        ActivityStatusAPI activityStatusAPI = new ActivityStatusAPI();
        activityStatusAPI.setPipelineId(glassMessage.getPipelineId());
        activityStatusAPI.setProcessId(glassMessage.getProcessId());
        activityStatusAPI.setGlobalId(glassMessage.getGlobalPId());
        activityStatusAPI.setGlassMessageId(UUIDGen.getCustomUUID());

        Status status = new Status();
        status.setDate(GlassMessageUtil.convertToXMLGregorianCalendar(new Date()));

        StringBuilder lensMessage = new StringBuilder().append(MAILBOX_ASA_IDENTIFIER);
        if (glassMessage.getCategory() != null) {
            lensMessage.append(" ");
            lensMessage.append(glassMessage.getCategory().name());
        }
        if (!MailBoxUtil.isEmpty(glassMessage.getProtocol())) {
            lensMessage.append(" ");
            lensMessage.append(glassMessage.getProtocol());
        }

        if (message != null && !message.equals("")) {
            status.setDescription(lensMessage.toString() + ": " + message);
        } else {
            status.setDescription(MAILBOX_ASA_IDENTIFIER);
        }
        status.setStatusId(UUIDGen.getCustomUUID());
        status.setType(statusType);

        activityStatusAPI.getStatuses().add(status);

        logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, activityStatusAPI);
    }

}
