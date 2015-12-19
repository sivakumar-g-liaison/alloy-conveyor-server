/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.services.unit.test;

import com.liaison.commons.messagebus.hornetq.jms.HornetQJMSRoundRobinSendClient;

/**
 *
 * @author OFS
 *
 */
public class DummyDropboxQueue extends HornetQJMSRoundRobinSendClient {

	 public static final String QUEUE_NAME = "dropboxQueue";
     private static DummyDropboxQueue ourInstance = new DummyDropboxQueue();

     public static DummyDropboxQueue getInstance() {
         return ourInstance;
     }

     private DummyDropboxQueue() {
        super(QUEUE_NAME);
     }
}
