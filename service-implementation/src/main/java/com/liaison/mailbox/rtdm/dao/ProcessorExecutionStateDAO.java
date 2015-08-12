/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import java.util.List;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;

/**
 * @author OFS
 *
 */
public interface ProcessorExecutionStateDAO extends GenericDAO<ProcessorExecutionState> {

	public static final String FIND_BY_PROCESSOR_ID = "ProcessorExecutionState.findByProcessorId";
	public static final String FIND_NON_EXECUTING_PROCESSORS = "ProcessorExecutionState.findNonExecutingProcessors";
	public static final String FIND_EXECUTING_PROCESSORS = "findExecutingProcessors";

	public static final String PROCESSOR_ID = "processorId";
	public static final String EXEC_STATUS = "exec_status";

	public ProcessorExecutionState findByProcessorId(String processorId);

	public void addProcessorExecutionState(String processorId, String executionStatus);

	public List <String> findNonExecutingProcessors();
	
	public List <String> findExecutingProcessors();

}
