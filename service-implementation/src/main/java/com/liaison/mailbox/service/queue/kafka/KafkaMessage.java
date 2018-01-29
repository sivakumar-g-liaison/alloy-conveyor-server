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
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.queue.kafka.KafkaMessageService.KafkaMessageType;
import com.liaison.usermanagement.service.dto.DirectoryMessageDTO;

public class KafkaMessage {

    private KafkaMessageType messageType;
    private WorkTicket filewriterWorkTicket;
    private DirectoryMessageDTO directoryMessageDTO;
    private Processor processor;

    public KafkaMessage(KafkaMessageType messageType, WorkTicket filewriterWorkTicket, DirectoryMessageDTO directoryMessageDTO, Processor processor) {
        this.messageType = messageType;
        this.filewriterWorkTicket = filewriterWorkTicket;
        this.directoryMessageDTO = directoryMessageDTO;
        this.processor = processor;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
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

    public void setMessageType(KafkaMessageType messageType) {
        this.messageType = messageType;
    }
}
