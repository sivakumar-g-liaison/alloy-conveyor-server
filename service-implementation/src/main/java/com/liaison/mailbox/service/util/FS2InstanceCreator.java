/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.io.IOException;

import com.liaison.fs2.api.FS2Factory;
import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.mailbox.service.core.processor.RemoteProcessorFS2Configuration;

/**
 * Common place to get the FS2 Instance.
 * 
 * @author veerasamyn
 */
public class FS2InstanceCreator {

	private static FlexibleStorageSystem FS2 = null;

	/**
	 * Instantiate the FS2. It gets the mount location from properties.
	 * 
	 * @return The FlexibleStorageSystem instance
	 * @throws IOException
	 */
	public static FlexibleStorageSystem getFS2Instance() throws IOException {

		if (null == FS2) {
			FS2 = FS2Factory.newInstance(new RemoteProcessorFS2Configuration());
		}
		return FS2;
	}
	
}
