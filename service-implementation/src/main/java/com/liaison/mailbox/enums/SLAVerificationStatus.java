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
 * @author OFS
 *
 */
public enum SLAVerificationStatus {

	SLA_VERIFIED("sla_verified"),
	SLA_NOT_VERIFIED("sla_not_verified"),
	SLA_NOT_APPLICABLE("sla_not_applicable");

	private final String code;

	private SLAVerificationStatus(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}

	public String getCode() {
		return code;
	}

	/**
	 * This  method will retrieve the SLA_Verifcation_Status by given sla status.
	 *
	 * @param code
	 *        The SLA verification status
	 * @return SLAVerificationStatus
	 */
	public static SLAVerificationStatus findByCode(String code) {

		SLAVerificationStatus found = null;
		for (SLAVerificationStatus value : SLAVerificationStatus.values()) {

			if (!MailBoxUtil.isEmpty(code) && code.equals(value.getCode())) {
				found = value;
				break;
			}
		}

		return found;
	}

	/**
	 * This  method will retrieve the SLAVerificationStatus by given sla status.
	 *
	 * @param name
	 *        The SLA verification status
	 * @return SLAVerificationStatus
	 */
	public static SLAVerificationStatus findByName(String name) {

		SLAVerificationStatus found = null;
		for (SLAVerificationStatus value : SLAVerificationStatus.values()) {

			if (!MailBoxUtil.isEmpty(name) && name.equals(value.name())) {
				found = value;
				break;
			}
		}

		return found;

	}


}
