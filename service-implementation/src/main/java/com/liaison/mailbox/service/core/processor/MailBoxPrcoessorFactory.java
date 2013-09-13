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

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.jpa.model.Processor;

/**
 * Factory class to instantiate MailBox Processors.
 * 
 * @author veerasamyn
 */
public class MailBoxPrcoessorFactory {

	/**
	 * 
	 */
	private MailBoxPrcoessorFactory() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Factory to method to create instances for mailbox processor.
	 * 
	 * @param processor
	 *            The Processor Entity
	 * @return The MailBox Processor instance.
	 */
	public static MailBoxProcessor getInstance(Processor processor) {

		MailBoxProcessor mailBoxProcessor = null;

		if (MailBoxConstants.REMOTE_DOWNLOADER.equals(processor.getProcessorType())) {
			mailBoxProcessor = new HttpRemoteDownloader(processor);
		}

		return mailBoxProcessor;
	}

}
