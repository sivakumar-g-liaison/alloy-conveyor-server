/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.configuration.request;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * @author santoshc
 * 
 */

@JsonRootName("searchMailboxRequest")
public class SearchMailboxRequestDTO {
	
	private String primaryServiceInstanceId;
	private List<String> secondaryServiceInstanceIds;
	
	public String getPrimaryServiceInstanceId() {
		return primaryServiceInstanceId;
	}
	public void setPrimaryServiceInstanceId(String primaryServiceInstanceId) {
		this.primaryServiceInstanceId = primaryServiceInstanceId;
	}
	public List<String> getSecondaryServiceInstanceIds() {
		if(secondaryServiceInstanceIds == null) {
			secondaryServiceInstanceIds = new ArrayList<String>();
		}
		return secondaryServiceInstanceIds;
	}
	public void setSecondaryServiceInstanceIds(List<String> secondaryServiceInstanceIds) {
		this.secondaryServiceInstanceIds = secondaryServiceInstanceIds;
	}
}
