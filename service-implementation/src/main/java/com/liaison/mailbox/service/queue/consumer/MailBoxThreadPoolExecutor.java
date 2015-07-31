/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.queue.consumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author OFS
 *
 */
public class MailBoxThreadPoolExecutor extends ThreadPoolExecutor{
    
    
	public MailBoxThreadPoolExecutor(int corePoolSize, int maximumPoolSize,	long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,boolean allowCoreThreadTimeOut) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
	}

}

