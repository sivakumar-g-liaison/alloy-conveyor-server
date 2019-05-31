/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;
import com.liaison.mailbox.service.dto.configuration.SweeperEventRequestDTO;
import com.liaison.mailbox.service.queue.sender.SweeperEventSendQueue;

public class SweeperEventService implements Runnable {

    private File file;
    private SweeperStaticPropertiesDTO staticProp;
    private Processor processor;

    private static final Logger LOGGER = LogManager.getLogger(SweeperEventService.class);

    public SweeperEventService() {

    }

    public SweeperEventService(File file, SweeperStaticPropertiesDTO staticProp, Processor process) {
        this.file = file;
        this.staticProp = staticProp;
        this.processor = process;
    }

    private void doProcess() throws Throwable {

        SweeperEventRequestDTO relateiveRelayRequestDTO = new SweeperEventRequestDTO(file, staticProp);
        relateiveRelayRequestDTO.setMailBoxId(processor.getMailbox().getPguid());
        relateiveRelayRequestDTO.setDynamicProperties(processor.getDynamicProperties());
        relateiveRelayRequestDTO.setTtlMap(processor.getTTLUnitAndTTLNumber());
        LOGGER.info("Relative Relay Downloader details1 {}", file);
        LOGGER.info("RElative Relay static property details {}", staticProp.getPipeLineID());
        LOGGER.info("Processor Details for {},  {},  {}", processor.getMailbox().getPguid(), processor.getProcsrName(),  processor.getPguid());
        String message = JAXBUtility.marshalToJSON(relateiveRelayRequestDTO);

        SweeperEventSendQueue.post(message);
    }

    @Override
    public void run() {
        try {
            doProcess();
        } catch (Throwable ex) {
           LOGGER.error("Exception caught {}", ex.getMessage());
        }
    }
}
