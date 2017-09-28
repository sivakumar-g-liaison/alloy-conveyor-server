/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.topic;


import com.liaison.commons.messagebus.topic.TopicPooledListenerContainer;
import com.liaison.commons.messagebus.topic.TopicTextMessageProcessor;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;

/**
 * Custom MailboxTopicPooled Listener
 */
public class MailBoxTopicPooledListenerContainer extends TopicPooledListenerContainer {

    private static final DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();
    private static final String PROPERTY_TOPIC_KEEP_ALIVE = "com.liaison.mailbox.topic.pool.keepalive.seconds";

    /**
     * Creates the instance of MailBoxTopicPooledListenerContainer
     *
     * @param messageProcessorClass processor instance
     * @param configurationSuffix   config suffix
     */
    public MailBoxTopicPooledListenerContainer(Class<? extends TopicTextMessageProcessor> messageProcessorClass, String configurationSuffix) {

        super(messageProcessorClass, configurationSuffix);

        // Sets the keep alive timeout for the threads in topic listener pool
        long DEFAULT_KEEP_ALIVE = 60000;
        container.setReceiveTimeout(configuration.getLong(PROPERTY_TOPIC_KEEP_ALIVE, DEFAULT_KEEP_ALIVE));
    }
}
