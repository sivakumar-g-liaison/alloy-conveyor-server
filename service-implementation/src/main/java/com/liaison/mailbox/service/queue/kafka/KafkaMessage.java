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
    private String fileDeleteMessage;
    private String dirAbsolutePath;
    private String datacenter;
    private String fileCreateMessage;

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

    public String getDirAbsolutePath() {
        return dirAbsolutePath;
    }

    public void setDirAbsolutePath(String dirAbsolutePath) {
        this.dirAbsolutePath = dirAbsolutePath;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public String getFileCreateMessage() {
        return fileCreateMessage;
    }

    public void setFileCreateMessage(String fileCreateMessage) {
        this.fileCreateMessage = fileCreateMessage;
    }
}
