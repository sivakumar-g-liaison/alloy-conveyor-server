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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class KafkaMessageService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(KafkaMessageService.class);
    private String message;

    public KafkaMessageService(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        LOGGER.info("KafkaMessageService : received message is :" + message);

        //TODO read the message type and invoke the particular method
        // The structure would be like MessageType(FILEWRITER_CREATE,USERACCOUNT_CREATE, USERACCOUNT_DELETE AND DIRECTORY_CREATION) and Message(string)
        //1. Filewriter notification - We will receive notification whenever file staged in other datacenter for the filewriter
        //We have to receive this notification and write the file to disk


        //2. User Directory Creation
        //Whenever machine user account created in other datacenter, that datacenter Relay would create directories and post notification to other datacenter.
        //This has to receive and create directories for that account

        //3. User Directory Deletion

        //4. Directory Creation notification
        //Whenever we create directories in Relay Process it would be redirected to other datacenter.
        //we have to receive and create directories here

    }

}
