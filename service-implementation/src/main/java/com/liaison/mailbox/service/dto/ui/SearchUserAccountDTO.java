/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.ui;

import java.util.List;

/**
 * 
 * 
 * @author praveenu
 */
public class SearchUserAccountDTO {

	private String guid;
	private String status;
	private String loginId;
	private List<String> providerName;
	
	private boolean incomplete;
	private String accountType;
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public List<String> getProviderName() {
		return providerName;
	}
	public void setProviderName(List<String> providerName) {
		this.providerName = providerName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public boolean isIncomplete() {
		return incomplete;
	}
	public void setIncomplete(boolean incomplete) {
		this.incomplete = incomplete;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getGuid() {
		return guid;
	}
}
