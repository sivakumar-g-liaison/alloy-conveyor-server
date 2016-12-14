/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.sla.MailboxWatchDogService;

/**
 * Test class to test mailbox watchdog service.
 * 
 * @author veerasamyn
 */
public class MailBoxWatchDogServiceIT extends BaseServiceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		System.setProperty("com.liaison.secure.properties.path", "invalid");
		System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
		System.setProperty("archaius.deployment.environment", "test");
		InitInitialDualDBContext.init();
	}

	/**
	 * This is just to check the flow and DB operations. Not intened to test full functionality
	 * @throws Exception 
	 * 
	 */
	@Test
	public void testMailBoxWatchDog() throws Exception {
		MailboxWatchDogService service = new MailboxWatchDogService();
		service.pollAndUpdateStatus();
	}
}
