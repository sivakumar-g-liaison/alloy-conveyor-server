/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor.helper;

import com.liaison.mailbox.service.core.processor.AbstractProcessor;

/**
 * Factory class to fetch the client.
 * 
 * @author OFS
 */
public class ClientFactory {

	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private static final String FTP = "ftp";
	private static final String FTPS = "ftps";
	private static final String SFTP = "sftp";

	/**
	 *
	 *
	 * @param procsrProtocol
	 * @param processor
	 * @return
	 */
	public static Object getClient(AbstractProcessor processorService) {

		String procsrProtocol = processorService.getConfigurationInstance().getProcsrProtocol();
		switch(procsrProtocol.toLowerCase()) {

			case HTTP:
				return HTTPClient.getClient(processorService);
			case HTTPS:
				return HTTPClient.getClient(processorService);
			case FTP:
				return FTPSClient.getClient(processorService);
			case FTPS:
				return FTPSClient.getClient(processorService);
			case SFTP:
				return SFTPClient.getClient(processorService);
			default: //It won't happen.
				return null;
		}
	}

}
