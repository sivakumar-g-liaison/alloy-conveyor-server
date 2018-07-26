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

import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;

/**
 * This mock is used to make sure that there's no actual kafka/queue service dependencies when using only HornetQ
 *
 */
public class KafkaMessageProcessorMock implements KafkaTextMessageProcessor {

    @Override
    public void processMessage(String message, String topic) {
    }

    @Override
    public void logConsumeEvent(String message, String topic) {
    }
}
