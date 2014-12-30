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

import com.liaison.mailbox.service.dto.CommonResponseDTO;

/**
 *
 * @author OFS
 */
public class GetExecutingProcessorResponseDTO extends CommonResponseDTO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<GetExecutingProcessorDTO> executingProcessors;
	private String hitCounter;

	public List<GetExecutingProcessorDTO> getExecutingProcessor() {

		if (null == executingProcessors) {
			executingProcessors = new ArrayList<GetExecutingProcessorDTO>();
		}
		return executingProcessors;
	}

	public void setExecutingProcessor(List<GetExecutingProcessorDTO> executingProcessors) {
		this.executingProcessors = executingProcessors;
	}

	public void setHitCounter(String hitCounter) {
		this.hitCounter = hitCounter;
	}
}
