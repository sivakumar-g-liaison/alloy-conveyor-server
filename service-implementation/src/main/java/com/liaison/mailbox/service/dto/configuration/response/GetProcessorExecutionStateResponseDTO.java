/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 * Data Transfer Object for updating and retrieving processor state functionality.
 *
 * @author OFS
 */
@JsonRootName("processorExecutionStateResponse")
public class GetProcessorExecutionStateResponseDTO extends CommonResponseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<String> executingProcessorIds = new ArrayList<String>();
	private int totalItems;

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}
	
	public List<String> getExecutingProcessorIds() {
		return executingProcessorIds;
	}
	public void setExecutingProcessorIds(List<String> executingProcessorIds) {
		this.executingProcessorIds = executingProcessorIds;
	};
}
