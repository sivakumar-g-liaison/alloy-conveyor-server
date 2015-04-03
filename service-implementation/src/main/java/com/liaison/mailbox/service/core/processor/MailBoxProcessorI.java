/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor;

import com.liaison.mailbox.service.core.fsm.MailboxFSM;

/**
 * @author Ganesh
 *
 */
public interface MailBoxProcessorI {

	public void runProcessor(String executionId,MailboxFSM fsm);

	public void createLocalPath();

}
