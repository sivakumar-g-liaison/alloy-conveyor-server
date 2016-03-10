/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.glass.util;

import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;

/**
 * Util for GlassMessage
 * 
 * @author VNagarajan
 *
 */
public class GlassMessageUtil {

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
            final String message) {

        TransactionVisibilityClient transactionVisibilityClient = new TransactionVisibilityClient();
        GlassMessage glassMessage = new GlassMessage();
        glassMessage.setGlobalPId(globalProcessId);
        glassMessage.setCategory(processorType);
        glassMessage.setProtocol(processProtocol);
        glassMessage.setStatus(status);
        glassMessage.setOutboundFileName(fileName);

        if (ExecutionState.COMPLETED.equals(status)) {
            glassMessage.setOutAgent(processProtocol);
            glassMessage.setOutSize(fileLength);

            // Fourth corner timestamp
            glassMessage.logFourthCornerTimestamp();
            glassMessage.logProcessingStatus(
                    StatusType.SUCCESS,
                    message,
                    processProtocol,
                    processorType.name());
        } else if (ExecutionState.FAILED.equals(status)) {
            glassMessage.logProcessingStatus(StatusType.ERROR,
                    message,
                    processProtocol,
                    processorType.name());
        } else if (ExecutionState.DUPLICATE.equals(status)) {
            glassMessage.setOutAgent(processProtocol);
            glassMessage.logProcessingStatus(StatusType.SUCCESS, message.toString(), processorType.name());
        } else if (ExecutionState.PROCESSING.equals(status)) {

            glassMessage.setInAgent(processProtocol);
            glassMessage.setInSize(fileLength);

            // Fourth corner timestamp
            glassMessage.logFirstCornerTimestamp();
            glassMessage.logProcessingStatus(
                    StatusType.RUNNING,
                    message,
                    processProtocol,
                    processorType.name());

            // Queued message for async inbound
            if (ProcessorType.HTTPASYNCPROCESSOR.equals(processorType)
                    || ProcessorType.SWEEPER.equals(processorType)) {
                glassMessage.logProcessingStatus(StatusType.QUEUED,
                        "Workticket queued for file " + fileName,
                        processProtocol,
                        processorType.name());
            }
        } else if (ExecutionState.READY.equals(status)) {
            glassMessage.setOutAgent(processProtocol);
            glassMessage.setOutSize(fileLength);
        }

        // TVAPI
        transactionVisibilityClient.logToGlass(glassMessage);
    }

}
