/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.kafka;

import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;

/**
 * Kafka Message
 */
public class KafkaMessage {

    private KafkaMessageType messageType;
    private String fileWriterMsg;
    private DirectoryMessageDTO directoryMessageDTO;
    private String processorGuid;
    private String fileDeleteMessage;

    public String getProcessorGuid() {
        return processorGuid;
    }

    public void setProcessorGuid(String processorGuid) {
        this.processorGuid = processorGuid;
    }

    public DirectoryMessageDTO getDirectoryMessageDTO() {
        return directoryMessageDTO;
    }

    public void setDirectoryMessageDTO(DirectoryMessageDTO directoryMessageDTO) {
        this.directoryMessageDTO = directoryMessageDTO;
    }

    public String getFileWriterMsg() {
        return fileWriterMsg;
    }

    public void setFileWriterMsg(String fileWriterMsg) {
        this.fileWriterMsg = fileWriterMsg;
    }

    public KafkaMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(KafkaMessageType directoryCreation) {
        this.messageType = directoryCreation;
    }

    public String getFileDeleteMessage() {
        return fileDeleteMessage;
    }

    public void setFileDeleteMessage(String fileDeleteMessage) {
        this.fileDeleteMessage = fileDeleteMessage;
    }
}
