/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.queue.consumer;

import com.liaison.commons.messagebus.queue.QueuePooledListenerContainer;
import com.liaison.commons.messagebus.queue.QueueTextMessageProcessor;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;

/**
 * Custom MailboxQueuePooled Listener
 */
public class MailboxQueuePooledListenerContainer extends QueuePooledListenerContainer {

    private static final DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private static final String PROPERTY_QUEUE_KEEP_ALIVE = "com.liaison.mailbox.queue.pool.keepalive.seconds";


    /**
     * Creates the instance of MailboxQueuePooledListenerContainer
     *
     * @param messageProcessorClass processor instance
     * @param configurationSuffix config suffix
     */
    public MailboxQueuePooledListenerContainer(Class<? extends QueueTextMessageProcessor> messageProcessorClass, String configurationSuffix) {

        super(messageProcessorClass, configurationSuffix);

        // Sets the keep alive timeout for the threads in queue listener pool
        long DEFAULT_KEEP_ALIVE = 60000;
        container.setReceiveTimeout(configuration.getLong(PROPERTY_QUEUE_KEEP_ALIVE, DEFAULT_KEEP_ALIVE));
    }
}
