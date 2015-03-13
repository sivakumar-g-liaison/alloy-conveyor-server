/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue;

import com.liaison.commons.messagebus.hornetq.HornetQRecoveringCoreSendClient;

/**
 *
 * @author OFS
 *
 */
public class DummyDropboxQueue extends HornetQRecoveringCoreSendClient {

	 public static final String QUEUE_NAME = "dropboxQueue";
     private static DummyDropboxQueue ourInstance = new DummyDropboxQueue();

     public static DummyDropboxQueue getInstance() {
         return ourInstance;
     }

     private DummyDropboxQueue() {
        super(QUEUE_NAME);
     }
}
