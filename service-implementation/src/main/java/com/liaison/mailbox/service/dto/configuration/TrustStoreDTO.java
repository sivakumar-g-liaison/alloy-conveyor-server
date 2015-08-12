/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;


/**
 * 
 * 
 * @author ofs
 */
@Deprecated
public class TrustStoreDTO {

	private String trustStoreId;
	private String trustStoreGroupId;
	
	public String getTrustStoreId() {
		return trustStoreId;
	}
	public void setTrustStoreId(String trustStoreId) {
		this.trustStoreId = trustStoreId;
	}
	public String getTrustStoreGroupId() {
		return trustStoreGroupId;
	}
	public void setTrustStoreGroupId(String trustStoreGroupId) {
		this.trustStoreGroupId = trustStoreGroupId;
	}
	
}
