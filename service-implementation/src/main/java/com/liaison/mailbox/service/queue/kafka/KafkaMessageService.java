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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.mailbox.dtdm.dao.MailboxDTDMDAO;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.core.processor.FileWriter;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorFactory;
import com.liaison.mailbox.service.core.processor.MailBoxProcessorI;
import com.liaison.mailbox.service.directory.DirectoryService;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;


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
                    LOGGER.debug("KAFKA_CONSUMER: FILEWRITER_CREATE" + kafkaMessage.getFilewriterWorkTicket().getFileName());
                    FileWriter fileWriter = new FileWriter(getProcessor(kafkaMessage.getProcessorGuid()));
                    fileWriter.writeReplicateData(kafkaMessage.getFilewriterWorkTicket());
                    
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
                    LOGGER.debug("KAFKA_CONSUMER: DIRECTORY_CREATION" + kafkaMessage.getProcessorGuid());
                    MailBoxProcessorI processorService = MailBoxProcessorFactory.getInstance(getProcessor(kafkaMessage.getProcessorGuid()));
                    if (processorService != null) {
                        processorService.createLocalPath();
                    }
                    break;
                
                default:
                    LOGGER.info("MessageType is not valid.");
                    break;
            }
        } catch (JAXBException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
     * Method to return processor by using processor guid.
     * 
     * @param processorGuid
     * @return
     */
    private Processor getProcessor(String processorGuid) {
        EntityManager em = null;
        Processor processor = null;

        try {
            em = DAOUtil.getEntityManager(MailboxDTDMDAO.PERSISTENCE_UNIT_NAME);
            processor = em.find(Processor.class, kafkaMessage.getProcessorGuid());

            if (processor == null) {
                throw new MailBoxConfigurationServicesException(Messages.PROCESSOR_DOES_NOT_EXIST,
                        kafkaMessage.getProcessorGuid(), Response.Status.BAD_REQUEST);
            }

        } finally {
            if (em != null) {
                em.close();
            }
        }

        return processor;
    }

}
