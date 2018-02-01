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

import com.liaison.dto.queue.WorkTicket;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;

/**
 *  Kafka Message
 */
public class KafkaMessage {

    private KafkaMessageType messageType;
    private WorkTicket filewriterWorkTicket;
    private DirectoryMessageDTO directoryMessageDTO;
    private String processorGuid;

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

    public WorkTicket getFilewriterWorkTicket() {
        return filewriterWorkTicket;
    }

    public void setFilewriterWorkTicket(WorkTicket filewriterWorkTicket) {
        this.filewriterWorkTicket = filewriterWorkTicket;
    }

    public KafkaMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(KafkaMessageType directoryCreation) {
        this.messageType = directoryCreation;
    }
}
