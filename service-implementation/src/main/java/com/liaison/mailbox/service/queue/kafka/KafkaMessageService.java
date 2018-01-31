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

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;


public class KafkaMessageService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(KafkaMessageService.class);
    private String message;
    private KafkaMessage kafkaMessage;

    public enum KafkaMessageType {
        FILEWRITER_CREATE,
        USERACCOUNT_CREATE,
        USERACCOUNT_DELETE,
        DIRECTORY_CREATION
    }

    public KafkaMessageService(String message) {
        this.message = message;
    }

    @Override
    public void run() {

        try {

            kafkaMessage = JAXBUtility.unmarshalFromJSON(message, KafkaMessage.class);
            
            LOGGER.info("KafkaMessageService : received message type :" + kafkaMessage.getMessageType().toString());

            switch (kafkaMessage.getMessageType()) {
                case FILEWRITER_CREATE:
                    LOGGER.info("KAFKA_CONSUMER: FILEWRITER_CREATE" + kafkaMessage.getFilewriterWorkTicket().getFileName());
                    /*WorkTicket workTicket = kafkaMessage.getFilewriterWorkTicket();
                    try (FileOutputStream outputStream = new FileOutputStream(workTicket.getFileName())) {
                        IOUtils.copy(StorageUtilities.retrievePayload(workTicket.getPayloadURI()), outputStream);
                    }*/
                    break;
                case USERACCOUNT_CREATE:
                    LOGGER.info("KAFKA_CONSUMER: USERACCOUNT_CREATE" + kafkaMessage.getDirectoryMessageDTO().getOperationType());
                    // new DirectoryService("").executeDirectoryOperation(kafkaMessage.getDirectoryMessageDTO(), false);
                    break;
                case USERACCOUNT_DELETE:
                    LOGGER.info("KAFKA_CONSUMER: USERACCOUNT_DELETE" + kafkaMessage.getDirectoryMessageDTO().getOperationType());
                    // new DirectoryService("").executeDirectoryOperation(kafkaMessage.getDirectoryMessageDTO(), false);
                    break;
                case DIRECTORY_CREATION:
                    LOGGER.info("KAFKA_CONSUMER: DIRECTORY_CREATION" + kafkaMessage.getProcessorGuid());
                    /*MailBoxProcessorI processorService = MailBoxProcessorFactory.getInstance(kafkaMessage.getProcessorGuid());
                    if (processorService != null) {
                        processorService.createLocalPath();
                    }*/
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
