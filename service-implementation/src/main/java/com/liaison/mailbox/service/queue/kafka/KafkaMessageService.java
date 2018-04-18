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

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.mailbox.service.core.FileDeleteReplicationService;
import com.liaison.mailbox.service.core.FileStageReplicationService;
import com.liaison.mailbox.service.directory.DirectoryService;
import com.liaison.mailbox.service.util.DirectoryCreationUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBException;

import java.io.IOException;

/**
 * To process message from kafka consumer.
 */
public class KafkaMessageService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(KafkaMessageService.class);
    private String message;

    public enum KafkaMessageType {
        FILEWRITER_CREATE,
        USERACCOUNT_CREATE,
        USERACCOUNT_DELETE,
        DIRECTORY_CREATION,
        FILE_DELETE
    }

    public KafkaMessageService(String message) {
        this.message = message;
    }

    @Override
    public void run() {

        try {

            KafkaMessage kafkaMessage = JAXBUtility.unmarshalFromJSON(message, KafkaMessage.class);
            LOGGER.info("KafkaMessageService : received message type - {} and the message is {}", kafkaMessage.getMessageType().toString(), message);

            switch (kafkaMessage.getMessageType()) {
                
                case FILEWRITER_CREATE:
                    LOGGER.debug("KAFKA_CONSUMER: FILEWRITER_CREATE" + kafkaMessage.getFileWriterMsg());
                    try {
                        FileStageReplicationService fileStageReplicationService = new FileStageReplicationService();
                        fileStageReplicationService.stage(kafkaMessage.getFileWriterMsg());
                    } catch (Throwable e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    break;
                case USERACCOUNT_CREATE:
                    LOGGER.debug("KAFKA_CONSUMER: USERACCOUNT_CREATE" + kafkaMessage.getDirectoryMessageDTO().getOperationType());
                    new DirectoryService("").executeDirectoryOperation(kafkaMessage.getDirectoryMessageDTO(), false);
                    break;
                case USERACCOUNT_DELETE:
                    LOGGER.debug("KAFKA_CONSUMER: USERACCOUNT_DELETE" + kafkaMessage.getDirectoryMessageDTO().getOperationType());
                    new DirectoryService("").executeDirectoryOperation(kafkaMessage.getDirectoryMessageDTO(), false);
                    break;
                case DIRECTORY_CREATION:
                    LOGGER.debug("KAFKA_CONSUMER: DIRECTORY_CREATION" + kafkaMessage.getDirAbsolutePath());
                    DirectoryCreationUtil.createPathIfNotAvailable(kafkaMessage.getDirAbsolutePath());
                    break;
                case FILE_DELETE:
                    LOGGER.debug("KAFKA_CONSUMER: FILE_DELETE" + kafkaMessage.getFileDeleteMessage());
                    new FileDeleteReplicationService().inactivateStageFileAndUpdateLens(kafkaMessage.getFileDeleteMessage());
                    break;
                default:
                    LOGGER.info("MessageType is not valid.");
                    break;
            }
        } catch (JAXBException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
