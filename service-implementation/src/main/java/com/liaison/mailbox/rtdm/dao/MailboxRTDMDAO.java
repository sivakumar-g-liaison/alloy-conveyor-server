/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

/**
 * Interface to hold the persistence unit name for runtime database.
 * 
 * @author OFS
 */
public interface MailboxRTDMDAO {
	String PERSISTENCE_UNIT_NAME = "mailbox-rtdm";
    String DATACENTER_NAME = System.getProperty("archaius.deployment.datacenter");
}
