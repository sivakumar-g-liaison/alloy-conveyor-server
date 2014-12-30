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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 *
 *
 * @author veerasamyn
 */
@JsonRootName("searchMailBoxResponse")
public class SearchMailBoxResponseDTO extends CommonResponseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<SearchMailBoxDTO> mailBox;
	private String hitCounter;
	private int totalItems;

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public List<SearchMailBoxDTO> getMailBox() {

		if (null == mailBox) {
			mailBox = new ArrayList<SearchMailBoxDTO>();
		}
		return mailBox;
	}

	public void setMailBox(List<SearchMailBoxDTO> mailBox) {
		this.mailBox = mailBox;
	}

	public String getHitCounter() {
		return hitCounter;
	}

	public void setHitCounter(String hitCounter) {
		this.hitCounter = hitCounter;
	}
}
