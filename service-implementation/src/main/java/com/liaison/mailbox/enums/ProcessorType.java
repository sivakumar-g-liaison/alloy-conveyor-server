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

public enum ProcessorType {

	REMOTEDOWNLOADER("remotedownloader"),
	REMOTEUPLOADER("remoteuploader"),
	SWEEPER("sweeper");

	private final String code;

	private ProcessorType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
    /**
     * This  method will retrieve the ProcessorType by given FSMState processorType.
     * 
     * @param code 
     *        The FSMState processorType 
     * @return ProcessorType
     */
	public static ProcessorType findByCode(String code) {

		ProcessorType found = null;
		for (ProcessorType value : ProcessorType.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}
    
	/**
	 * This  method will retrieve the ProcessorType by given processorType from ProcessorDTO.
	 * 
	 * @param name  
	 *        The ProcessorDTO processorType
	 * @return ProcessorType
	 */
	public static ProcessorType findByName(String name) {

		ProcessorType found = null;
		for (ProcessorType value : ProcessorType.values()) {

			if (!MailBoxUtil.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}

}
