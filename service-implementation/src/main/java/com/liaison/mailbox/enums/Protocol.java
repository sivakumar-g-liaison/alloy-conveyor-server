/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.enums;

import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Contains the list of protocols.
 * 
 * @author OFS
 */
public enum Protocol {

	FTP("ftp"),
	FTPS("ftps"),
	SFTP("sftp"),
	HTTP("http"),
	HTTPS("https"),
	SWEEPER("sweeper"),
	CONDITIONALSWEEPER("conditionalsweeper"),
	LITEHTTPSYNCPROCESSOR("litehttpsyncprocessor"),
	HTTPSYNCPROCESSOR("httpsyncprocessor"),
	HTTPASYNCPROCESSOR("httpasyncprocessor"),
	FILEWRITER("filewriter"),
	DROPBOXPROCESSOR("dropboxprocessor");

	private final String code;

	private Protocol(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
    
	/**
	 * This  method will retrieve the Protocol by given Processor protocol.
	 * 
	 * @param code 
	 *        The Processor protocol
	 * @return Protocol
	 */
	public static Protocol findByCode(String code) {

		Protocol found = null;
		for (Protocol value : Protocol.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}
    
	/**
	 * This  method will retrieve the Protocol by given protocol from ProcessorDTO.
	 * 
	 * @param name 
	 *        The ProcessorDTO protocol
	 * @return Protocol
	 */
	public static Protocol findByName(String name) {

		Protocol found = null;
		for (Protocol value : Protocol.values()) {

			if (!MailBoxUtil.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
